# Phase 0 Research: 核心阶段 Agent OS 运行时内核

**Feature**: `001-core-agent-os-kernel` | **Date**: 2026-10-27

本文档消解 plan.md Technical Context 中的全部技术未知项，并记录关键决策的取舍依据。所有决策均受 `constitution.md` v2.0.1 约束。

---

## D-01 ReAct 循环中如何"只借 Spring AI 的管道，不交控制权"

**Decision**: 通过 `ChatModel.call(Prompt)` 获取 `ChatResponse`，**由 `ReActLoop` 自行解析** `AssistantMessage.getToolCalls()` 并调度，不使用 `ChatClient` 的 `tools()`/`toolCallbacks()` 自动执行链。

**Rationale**: 宪法 I/II 要求循环与工具执行独占控制权。Spring AI 的 `ChatClient` 在注册 tool callbacks 后会自行解析并执行工具再回灌，这正是 CLAUDE.md 错误清单 #1「tool 被调两次」的根因。

**Alternatives considered**:

- `ChatClient` + `ToolCallbacks`：代码量最少，但循环控制权外泄，且工具执行无法挂接 `SandboxChecker` 审计时序 → 否决。
- 完全不用 Spring AI、自写各厂商 HTTP 客户端：违反技术栈声明（原则 II 明确保留协议转换职责），维护成本高 → 否决。

**落地约束**:

1. 启动类排除 `DashScopeAutoConfiguration` 等厂商自动装配，避免无 key 时阻断启动。
2. `ToolCallAdapter` **只**把 `OryxTool.getInputSchema()` 转成 Spring AI 的 `ToolDefinition`（schema 生成），不注册执行器。
3. 单测断言：一次 tool call 只产生一条 `tool_invocations`（防重复执行回归）。

---

## D-02 Provider 显式映射的构建方式

**Decision**: 由 `application.yaml` 的 `oryxos.providers[*]` 显式声明，`OryxOsConfiguration` 逐个构建 `ChatModel` 实例并组装 `ProviderRegistry`（`Map<String, ProviderEntry>`）。

**Rationale**: 宪法 III。多 Provider 并存时 Spring 容器内多个 `ChatModel` Bean 类型相同、Bean name 未必等于 provider name，类型扫描必然产生歧义。

**Alternatives considered**:

- `context.getBeansOfType(ChatModel.class)` + `@Qualifier`：看似省事，但 Bean name 与 provider name 的绑定隐式且脆弱 → 否决。
- 依赖 Spring AI 的 `ChatModel` 自动装配再做名字对齐：与 D-01 的排除策略冲突，且无法覆盖同厂商多实例 → 否决。

**落地约束**:

- `ProviderRegistry` 构造期检测重名，重复即启动失败（fail-fast）。
- `require(name)` 失败抛 `ProviderNotRegisteredException`，消息含 agent 名、字段路径、已注册集合（FR-005/FR-008）。
- 核心阶段**不做** fallback / circuit breaker / hedge racing（FR-010）。

---

## D-03 `ProviderService` 签名扩展（决策 A：直接改签名）

**Decision**: 将 `String call(Profile, String prompt)` **直接替换**为 `ChatResponse call(Profile, Prompt prompt)`，`ChatResponse` 携带 `content`、`toolCalls: List<ToolCall>`、`InvocationInfo`（provider/model/token 用量/延迟）。**不保留** `@Deprecated` 旧方法。

**Rationale**:

- 旧签名只回传字符串，`ReActLoop` 无从得知「模型是否要求调工具、要调哪个、参数是什么」，无法满足 FR-011~013。
- 骨架阶段的唯一调用方是 `ReActLoop` 自身（`TODO(US-2)`），无外部消费方，**替换无兼容负担**。
- 保留双入口会让 Tool 执行路径出现两条，正是原则 I 要避免的「控制权分裂」。用户已确认选择 A。

**Alternatives considered**:

- **B：新增重载 + 旧方法 `@Deprecated`**：看似平滑，但会让「返回字符串」的旧路径长期存在，易被后续代码误用，导致 tool call 静默丢失 → 否决（用户已选定 A）。

**落地约束**:

- `InvocationInfo` 由 `DefaultProviderService` 在调用前后计时与取用量，供 `llm_calls` 落库（FR-043）。
- `ToolCall.callId` 必须原样透传，回填时作为 tool 消息的 `tool_call_id`，否则部分厂商（OpenAI 协议）会拒绝后续请求。

---

## D-04 工具来源三档统一（全局池 / MCP / Agent 私有）

**Decision**: 统一用 `ToolRegistry` 维护「工具实例 + 来源枚举」，再由 `AgentToolView.resolve(profile, globalPool)` 对**每个 Agent 装配期**产出可见视图 = `全局池 ∩ profile.tools` ∪ `Agent 目录私有工具`。

**Rationale**: FR-030~030c 要求「合并、归一、冲突报错、同等待遇」。在装配期合并（而非调用期临时判断）可以让冲突在启动/加载时暴露，符合 fail-fast。

**Alternatives considered**:

- 调用期直接查 `ToolRegistry`：实现最简单，但 Agent 私有工具会全局可见，违反 FR-030 隔离要求 → 否决。
- 每个 Agent 建独立 `ToolRegistry` 副本：内存冗余，且 `tool list` 等全局查询需再合并 → 否决。

**落地约束**:

1. 私有工具定义缺失/不可达/描述非法 → 报错并指出所属 Agent（FR-030c），**不静默丢弃**。
2. 同名冲突 → 抛异常（FR-030a），不覆盖。
3. 无论来源，执行都走同一 `ToolExecutor` 管线（沙箱 + 审计），FR-030b。

---

## D-05 `SandboxChecker` 真实路径校验（重写骨架版）

**Decision**: 重写 `checkFilePath`，采用 `toRealPath()` 解析 + 白名单根同样 `toRealPath()` 后 `startsWith` 比对；新建场景解析「最近存在父目录」的真实路径；对外返回 `Path realPath` 供工具直接使用。

**Rationale**: 宪法 VI 明确「字符串形式的 `normalize()+startsWith()` 不足以作为软连接场景的安全判定」。现有骨架正是这种不安全写法：`.oryxos/agents/a/skills/x → /etc` 在字符串层面仍以 `.oryxos/` 开头。

**Alternatives considered**:

- 仅 `normalize()` + `startsWith()`：被软连接与 `..` 绕过 → 否决（现状即此）。
- 逐段 `Files.isSymbolicLink()` 检查：需自行处理跨平台差异与竞态，且易漏 `toRealPath` 已覆盖的硬链接/挂载点场景 → 否决。
- `SecurityManager`：JDK 21 已不可用 → 违反宪法 VI，直接排除。

**落地约束**:

1. 校验返回的 `realPath` 必须被工具直接用于 I/O，禁止再次 `Path.of(原始输入)`（TOCTOU 窗口）。
2. Shell 侧补充**结构符拒绝**（`| ; & > < $( \``），否则首 token 白名单可被 `git; rm -rf /` 绕过——这是骨架版的已知缺口。
3. HTTP 侧拒绝 userinfo（`http://user@evil.com`）、非 http/https scheme、字面 IP（除非白名单显式含）。

---

## D-06 `AgentLoader` / `ContextLoader` 与 Skill 软连接绑定

**Decision**: `AgentLoader.deriveProfile` 每次从 `.oryxos/agents/<name>/AGENT.md` 的 frontmatter 派生 `Profile`；`ContextLoader` **每轮现读** `AGENT.md` 正文，并扫描 `agents/<name>/skills/` 下的相对软连接，只注入 `name` + `description` + 本地绝对读取路径。

**Rationale**: 宪法 IV——「目录配置即 Agent」且「唯一绑定真相源是软连接」，不得缓存正文（改文件应立即生效），不得预载 SKILL.md 正文（渐进披露，正文仅通过 `read_file` 按需进入上下文）。

**Alternatives considered**:

- 缓存 `Profile` 与正文以省 IO：改文件不生效，违背「每次现读」硬约束 → 否决。
- 用 `AGENT.md` frontmatter `skills:` 列表表达绑定：宪法 IV 明令禁止并行索引（会造成与目录漂移）→ 否决。
- 新增 `use_skill` 工具：宪法 IV 明文禁止，Skill 是上下文资源不是 Tool → 否决。

**落地约束**:

- 一致性检查识别 `dangling / escaped / invalid-target / name-mismatch / stale-reference` 五类（FR-034）。
- 软连接必须是**相对**、目标在 `.oryxos/skills/` 真实路径内、不允许多级链。
- 删除仍被引用的公共 Skill 默认拒绝并回列引用 Agent（不得制造悬空链接）。

---

## D-07 表结构管理：Flyway 取代 `ddl-auto=update`

**Decision**: 在 `oryxos-storage` 引入 Flyway，脚本 `classpath:db/migration/V1__init_core_tables.sql`；`spring.jpa.hibernate.ddl-auto` 由 `update` 改为 `validate`；开启 `baseline-on-migrate=true`。

**Rationale**: 宪法 VIII 明确「表结构变更 MUST NOT 依赖 Hibernate 自动迁移（SQLite `ALTER TABLE` 支持弱）」，且技术栈行已声明「Flyway 迁移目录（025 起）」。当前 `application.yaml` 仍是 `ddl-auto: update`，属**既有配置与宪法不符**，本特性一并修正。

**Alternatives considered**:

- 继续 `ddl-auto=update`：SQLite 上无法演进列（不支持 `ALTER COLUMN`），后续必然返工 → 否决。
- 手写启动期建表脚本（无版本管理）：无版本号、无幂等保证、无法审计演进历史 → 否决。
- Liquibase：功能等价但 XML/YAML 冗余，项目已倾向 Flyway → 否决。

**落地约束**:

- 已存在的 `oryxos.db` 靠 `baseline-on-migrate=true` 平滑接管。
- Flyway 迁移在 JPA `validate` 之前执行（Boot 默认顺序），启动失败即暴露 schema 漂移。
- 后续任何列变更必须新增 `V2__...` 而非改 `V1`。

---

## D-08 虚拟线程与同步阻塞的具体落地

**Decision**: `spring.threads.virtual.enabled=true`；全部 Service 方法同步返回；LLM 调用、工具执行、DB 写入均阻塞式。

**Rationale**: 宪法 VII。引入 `CompletableFuture`/Reactor 会让 `ReActLoop` 的「思考-执行-回填」顺序难以推理，且审计写入顺序无法保证。

**Alternatives considered**: WebFlux + Reactor（并发模型更强但调试成本高、与 JPA 阻塞驱动冲突）→ 否决；`CompletableFuture` 编排（同样是异步模型）→ 否决。

**落地约束**:

- 60s 超时通过调用侧 `Duration` 与 `GlobalExceptionHandler` 映射 504，不使用异步取消。
- 禁止在依赖图中引入 `spring-boot-starter-webflux`（ArchUnit 测试守护）。

---

## D-09 审计写入位置与粒度

**Decision**: `AuditRecorder` 在 `ToolExecutor`（工具）与 `DefaultProviderService`（LLM）内部**同步**写入；失败/拒绝同样写。

**Rationale**: 宪法 V 要求 Day One 落库且不可省。放在执行入口而非 Controller，保证 CLI 与 Web 两个通道都覆盖。

**Alternatives considered**: AOP 切面统一埋点（隐式、难保证「失败也写」）、日志后置批量入库（会丢记录且违反原则 VII）→ 均否决。

**落地约束**: 沙箱拒绝写 `status=DENIED`；工具异常写 `FAILED` 并保留 `error`；超 64KB 的 `result`/`error` 截断并标记 `...[truncated]`。

---

## 未知项消解汇总

| Technical Context 项 | 状态 |
| --- | --- |
| Spring AI 工具执行边界 | ✅ D-01 已定 |
| Provider 映射构建方式 | ✅ D-02 已定 |
| `ProviderService` 签名 | ✅ D-03 已定（用户选 A） |
| 工具来源合并 | ✅ D-04 已定 |
| 沙箱真实路径校验 | ✅ D-05 已定 |
| Skill 绑定与渐进披露 | ✅ D-06 已定 |
| 表结构迁移 | ✅ D-07 已定 |
| 并发模型 | ✅ D-08 已定 |
| 审计写入位置 | ✅ D-09 已定 |

**无 NEEDS CLARIFICATION 残留。**
