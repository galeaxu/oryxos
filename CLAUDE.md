# CLAUDE.md

本文件为 Claude Code 提供 OryxOS 项目的开发指引。所有在本仓库工作的 AI agent 都应先读本文件，再读 `docs/` 下的三份源文档。

## 项目是什么

OryxOS 是**一个企业能完全掌控的、Java 原生的、私有可审计的 Agent 统一底座**（Agent OS 运行时内核）。

- 装在企业自己的 K8s、服务器或物理机上，作为统一底座跑各种业务 Agent（运维、客服、HR、销售、知识管理等）。
- 业务方通过配置 Profile 定义 Agent，通过 SKILL.md + MCP 或 @Tool 注解写工具，**不写 Agent 后端代码**。
- 数据完全留在企业基础设施，不锁任何云生态。

**核心阶段定位**：交付的是 Agent OS 的**运行时内核**，能力上对齐业界开源 Agent OS 的基础层。让 OryxOS 成为真正企业级 Agent OS 的治理层（多租户、SSO、完整审计、Tool 治理、Web 仪表板）在**扩展和社区阶段**补齐。不要在本阶段把 OryxOS 包装成完整的企业级 Agent OS。

**锚点**：OryxOS 锚在"严监管企业需要一个自己完全掌控的 Agent 底座"这个不变的企业刚需上，不锚在"Agent OS 这个概念"上。

## 必读源文档（`docs/`）

| 文档 | 回答 | 用途 |
| --- | --- | --- |
| `docs/IndustryResearch.md` | Why（为什么做） | 定位与愿景，边界判断 |
| `docs/DemandAnalysis.md` | What（做什么） | 五大核心能力、验收标准（第 13 章） |
| `docs/TechnicalSolution.md` | How（怎么做） | 技术栈、模块结构、关键决策 |
| `docs/AiProgrammingGuide.md` | 怎么落地 | Spec-Kit 实施拆解、5 个 user story |

**以技术方案为准**：9 个 Maven 模块、五大核心能力。若文档间冲突，技术方案优先级最高。

## 技术栈（不可协商）

1. **JDK 21 + Spring Boot 3.x**，Maven 多模块（9 个模块），单二进制部署
2. **Spring AI + Spring AI Alibaba**（LLM Provider 抽象，复用现成 connector）
3. **自实现 ReAct loop**（Agent 核心循环）
4. **Spring MVC**（HTTP API 服务层，用 Java 21 virtual thread）
5. **Picocli**（命令行工具）
6. **SnakeYAML**（Profile YAML 解析）
7. **SQLite + Spring Data JPA**（Session、审计和元数据持久化）
8. **MCP Java SDK**（MCP Client 集成，社区项目，可能需部分自实现）
9. **Logback + SLF4J**（结构化日志）
10. **Micrometer + Prometheus**（指标采集，扩展阶段）

## 项目宪章（Non-Negotiable Principles）

以下原则在每次 specify / plan / tasks / implement 时都必须遵守。**七条原则中，原则四和原则六是最容易被 AI agent 写错、又必须守住的**。

### 原则一：单体 + Maven 多模块（9 个）
JDK 21 + Spring Boot 3.x 单体应用，9 个 Maven 模块，单二进制部署。扩展阶段引入 GraalVM Native Image。

### 原则二：五大核心能力优先
五大核心能力（对接 LLM、ReAct 循环、Memory、Tool、Web Service）优先，支撑模块次之。核心阶段交付运行时内核，企业级治理层放扩展阶段。

### 原则三：自实现 ReAct loop
自己写 ReAct loop，**不直接用 Spring AI 的 Agent 抽象**。保证 Agent 核心完全可控。核心循环约数十行 Java。

### 原则四：Spring AI 只用一半（最易写错）
这是最容易埋 bug 的地方。**Spring AI 在 OryxOS 里只做协议适配器和 schema 生成器，不做循环引擎。**

- **只使用**：① Provider 抽象和向各家 LLM 的协议转换；② @Tool 注解的 JSON Schema 生成。
- **必须禁用**：Spring AI 自带的 tool calling 自动执行机制。Tool 的调度和执行**完全由 OryxOS 自己的 ReActLoop + ToolExecutor 控制**。
- **否则会出现 tool 被调两次的 bug。**

### 原则五：Plugin Tool 三档接入，主推零代码
Plugin Tool 三种接入方式，按门槛从低到高：① 零代码 SKILL.md + 复用 MCP（**主推**）；② 轻代码自写 MCP server；③ 重代码 Java @Tool 注解。选择标准：能用一就不用二，能用二就不用三。

### 原则六：SQLite + MEMORY.md，审计 day one 落库（最易写错）
核心阶段持久化用 **SQLite + Spring Data JPA**，Memory 长期记忆用 **MEMORY.md 文件 + 关键词检索**（不用向量库）。

- **审计表 day one 落库**：`tool_invocations` 和 `llm_calls` 两张表在核心阶段就做**写入**（查询接口可放扩展阶段），让"可审计"这个差异化能力的数据地基从一开始就立起来，避免后期从日志反解析返工。AI agent 容易觉得"日志够了"而漏掉，必须落实。

### 原则七：每个 user story 有可演示 demo
每个 user story 完成后有可演示 demo，优先级是"跑通"而非"完美"。对应需求文档第 13 章的 5 个验收 demo。

## 架构总览

OryxOS 是 Spring Boot 单体应用，对外两个入口（CLI Channel、Web Service），消息都汇入同一个引擎。

```
接入层   CLI Channel            Web Service (REST API)
          \                    /
引擎层      ReActLoop + PromptBuilder + ToolExecutor   ← Agent 大脑
           /          |            \
能力层  Provider    Memory         Tool
        (LLM)      (三层记忆)     (内置 + Plugin)
           \          |            /
基础层  Profile/Bootstrap/Skill 加载、Session 存储、SQLite、配置与密钥加载
```

**一句话**：Provider、Memory、Tool 三个能力供养 ReAct 循环这个引擎，引擎通过 CLI 和 Web Service 两个入口对外提供。

### 五大能力协作关系
- **ReAct 循环**（引擎）：驱动"接收消息→组装 Prompt→调 LLM→执行 Tool→回填结果→继续推理"。
- **Provider**：每轮思考都要调一次 LLM。
- **Memory**：每轮组装 prompt 时注入会话历史和长期记忆。
- **Tool**：LLM 决定调哪个 Tool 后由 ReAct 循环执行。
- **Web Service**：前四个能力的对外出口，触发入口和结果出口之一（另一个是 CLI）。

## Maven 模块结构（9 个）

| 模块 | 对应 | 职责 |
| --- | --- | --- |
| `oryxos-core` | 核心引擎 | ReActLoop、PromptBuilder、ToolExecutor、ContextLoader、Session、Profile、OryxTool 抽象。所有模块都依赖它 |
| `oryxos-provider` | 能力一 | ProviderService、Function Calling 适配、provider name 映射 |
| `oryxos-memory` | 能力三 | MemoryService（三层统一门面）、LongTermMemory、MemoryTools |
| `oryxos-tool` | 能力四 | 内置 Tool（File/Shell/Http）、MCP Client、ToolRegistry、SandboxChecker（**三合一**） |
| `oryxos-web` | 能力五 | WebServer、六个 ApiController、GlobalExceptionHandler、OpenAPI 文档 |
| `oryxos-channel-cli` | 支撑 | CLI Channel 实现 |
| `oryxos-storage` | 支撑 | SQLite 存储层，含 sessions、tool_invocations、llm_calls 三张表 |
| `oryxos-cli` | 支撑 | Picocli 命令行入口（12 个子命令） |
| `oryxos-boot` | 支撑 | Spring Boot 启动模块，打 fat JAR |

模块之间通过**接口耦合**。扩展阶段加新 Channel / 新 Tool 只加新模块，改 core。所有 Channel 模块底层都调 `oryxos-web` 的 Agent 接口。

**打包**：`mvn clean package` 生成 fat JAR，`java -jar` 启动。

### 模块边界红线（易错）
- Memory 是 **MemoryService 三层统一门面**，不要简化成跟 Session 合并。
- Tool 相关**合并为一个 `oryxos-tool` 模块**（内置 Tool、MCP Client、ToolRegistry、Sandbox 都在里面），**不要拆成 builtin/skill/mcp 三个模块**。
- **SKILL.md 不是 Tool**，它是注入 system prompt 的指令模板，归 `oryxos-core` 的 **ContextLoader**，不归 Tool 模块（跟 Bootstrap 文件同类）。

## 关键实现约定

### Provider（能力一）
- **显式映射，不靠类型扫描**：ProviderService 维护一份显式的 `provider name → ChatModel` 映射表，不能靠"扫描容器里所有 ChatModel"自动区分（多 Provider 并存时 Bean 类型相同会有歧义）。
- 核心阶段不做 fallback 和 hedge racing，Provider 故障直接报错给 Agent。
- 每次 LLM 调用记录 token 使用量、Provider、模型，写入 `llm_calls` 表。

### ReAct 循环（能力二）
- 算法：用户消息追加到 Session → 组装 Prompt → 调 LLM → 无 Tool 调用则返回最终响应 → 有 Tool 调用则执行并回填 → 回到组装 Prompt → 达到最大迭代次数（默认 10，Profile 可覆盖）强制结束。
- `MAX_ITERATIONS` 默认 10，防止死循环。
- 每次迭代把 LLM 响应和 Tool 结果追加到 Session messages，保证完整调用链可查可审计。
- 上下文长度管理：保留 system prompt + 最近 N 轮（默认 20 轮，Profile 配置），超出丢弃。
- 核心阶段不做：Tool 并行调用、Agent 间任务委托、流式响应。

### Memory（能力三）
- **MemoryService 是三层记忆的统一门面**，ReAct 循环只调它一个接口。内部把会话记忆委托给 SessionManager，长期记忆委托给 LongTermMemory。
- LongTermMemory 四个方法：`append`、`load`、`recallByKeyword`、`truncateIfNeeded`（超 4000 字保留最近内容）。
- 接口预留向量检索升级空间：`recallByKeyword` 设计成可升级为带 mode 参数的 `recall`。
- MemoryTools：`save_memory`、`recall_memory` 两个内置 Tool（@Tool 注解）。
- MEMORY.md 每次重新读不缓存，Agent 调 save_memory 后下一轮立刻可见。
- **USER.md vs MEMORY.md**：USER.md 是 Bootstrap 文件（用户手写、只读、初始设定）；MEMORY.md 是长期记忆（Agent 通过 save_memory 写、读写、成长记录）。
- 核心阶段不做：自动抽取、语义检索、情景记忆、Memory Wiki、压缩（超长简单截断）。

### Tool（能力四）
- **OryxTool 抽象**：四个方法 `getName`、`getDescription`、`getInputSchema`（JSON Schema）、`execute`（JSON 输入 → ToolResult）。ToolResult 含成功标识、结果内容、错误信息、是否可重试。ReAct 循环不感知 Tool 来源。
- **内置 Tool 五个**：FileTools（read_file、write_file、list_dir）、ShellTools（shell）、HttpTools（http_get、http_post），加上 MemoryTools（save_memory、recall_memory 归 Memory 模块但作为内置 Tool 注册）。
- **ToolRegistry**：启动时扫描 @Tool 注解方法（内置 + 方式三），加上 MCP Client 注册的工具（方式二），全部包装成 OryxTool。Profile 按 tools 字段过滤子集。
- **SandboxChecker** 三个方法：`checkFilePath`、`checkShellCommand`、`checkHttpUrl`。校验失败抛 `SandboxViolationException`。
- **不使用 Java SecurityManager**（JDK 21 已不可用）。

### Web Service（能力五）
- 核心阶段 **10 个端点**：
  - 会话管理 4 个：POST /api/v1/sessions、POST /api/v1/sessions/{id}/messages、GET /api/v1/sessions/{id}、DELETE /api/v1/sessions/{id}
  - Agent 调用 1 个：POST /api/v1/agents/{name}/invoke
  - 信息查询 3 个：GET /api/v1/profiles、GET /api/v1/memory、GET /api/v1/tools
  - 系统状态 2 个：GET /api/v1/health、GET /api/v1/info
- 六个 Controller：SessionApiController、AgentApiController、ProfileApiController、MemoryApiController、ToolApiController、SystemApiController。每个只做参数校验、响应包装、错误处理。
- 错误码：标准 HTTP 状态码 + 内部错误码（400/404/500/503）。
- 核心阶段不做：认证、SSE 流式、WebSocket、RBAC、限流。
- 单条消息最大 32KB，Session 历史返回最多 100 条，Agent 调用 60 秒超时返回 504。

### 支撑模块
- **InitCommand**：`oryxos init` 创建 `.oryxos/` 工作目录（profiles/、memory/MEMORY.md、skills/、mcp_servers.yaml、sessions/、logs/、AGENTS.md/SOUL.md/USER.md、oryxos.db）。
- **Profile YAML** 字段：name、description、identity（agent_name、prompt）、provider（name、model、temperature）、tools、skills、mcp_servers、channels、bootstrap、settings（max_iterations、max_history_turns）。
- **ContextLoader**：合并 Bootstrap 文件加载 + Skill 文件加载（两者本质相同，都是注入 system prompt 的 markdown 上下文）。每次组装 prompt 时重新加载不缓存。
- **ConfigLoader**：统一加载敏感配置（LLM API key、Provider 凭证、MCP server 凭证）。通过环境变量注入，Profile 里用 `${ENV_VAR}` 占位，不明文写死。基础必填项和格式校验。
- **三种运行模式**：`oryxos chat`、`oryxos serve`、`oryxos gateway`，共享同一份 Profile 和 Session 存储。
- **CLI 12 个子命令**：init、status、chat、serve、gateway、profile list/create/show/delete、provider list、tool list、session list。

### 数据持久化
- SQLite 三张表：`sessions`（Session 元数据 + JSON 对话历史）、`tool_invocations`、`llm_calls`。
- **工程风险**：SQLite 的 ALTER TABLE 能力有限，`hibernate.ddl-auto=update` 对表结构演进支持很弱。首次建表可用 update，后续演进需手动维护建表脚本或引入 Flyway/Liquibase。
- 文件系统数据（不进 SQLite）：Profile YAML、Bootstrap 文件、MEMORY.md、SKILL.md、MCP 配置、日志。

### 工程约定
- 同步阻塞执行模型 + Java 21 virtual thread。
- 结构化日志（Logback + SLF4J）。
- SQLite 落 `.oryxos/oryxos.db`。

## 实施路径（5 个 User Story）

按依赖顺序推进，**不按时间拆**。具体时间由项目方定（需求/技术方案定的节奏是 4 周每周 3 小时）。

| User Story | 核心能力 | 依赖 | 验收 demo |
| --- | --- | --- | --- |
| US-1 | 对接 LLM | 无（基础） | 与 US-2 合并 → Demo 一 |
| US-2 | ReAct 循环 | US-1 | Demo 一（查天气穿衣） |
| US-3 | Memory 三层记忆 | US-2 | Demo 二（跨对话记偏好） |
| US-4 | Plugin Tool 体系 | US-2（与 US-3 并行） | Demo 三（零代码 PR digest） |
| US-5 | Web Service | 前 4 个 | Demo 四 + 五（同步调用、多端点联动） |

**推进顺序**：US-1 → US-2 →（US-3 + US-4 并行）→ US-5。

### 各 user story 要点

**US-1 对接 LLM**：模块 core + provider + boot。重点：ProviderService 显式 name 映射（不靠类型扫描）；ConfigLoader 从环境变量加载 key。完成后无独立 demo，随 US-2 一起验。

**US-2 ReAct 循环**：模块 core + tool（1 个 HTTP Tool + SandboxChecker 简化版）+ channel-cli + cli。重点：**禁用 Spring AI 自动 tool 执行**（否则 tool 被调两次）。

**US-3 Memory**：模块 memory。重点：MemoryService 三层统一门面；不破坏 US-2 的 ReAct 循环。

**US-4 Plugin Tool**：模块 tool（补齐 File/Shell Tool、MCP Client、完整 SandboxChecker、ToolRegistry）+ core（SKILL.md 归 ContextLoader）。重点：MCP Java 生态成熟度低，先实现 stdio transport（最常用），SSE 放扩展；先测连通性再集成。

**US-5 Web Service**：模块 web + storage + cli + core。重点：**审计表 tool_invocations、llm_calls 落库**（不是只放日志）；Session 从内存版升级到 SQLite；6 个 Controller 可并行。

## 工作流约定

### 主体开发用 Spec-Kit
- 已有需求文档 + 技术方案直接喂给 Spec-Kit，**不重写**。
- `需求文档 → /speckit.specify`（转成 5 个 user story）；`技术方案 → /speckit.plan`；需求文档第 3 章 + 技术方案 1.1 节 → `constitution.md`；需求文档第 13 章 5 个 demo → acceptance criteria。
- **每个 user story 完成后必须跑 `/speckit.analyze`**，做跨 artifact 一致性检查，不能省。
- 增量阶段（社区接力）切换到手动提示词 + Claude Code，小增量不必走完整 Spec-Kit 流程。

### AI agent 跑偏时的纠正
看到生成的代码不符合上述原则（尤其是**启用 Spring AI 自动 tool 执行、Provider 用类型扫描、Tool 拆成多模块、SkillLoader 当成 Tool、审计表没落库、Memory 简化成跟 Session 合并**），主动让 AI agent 重读本文件改正。

### Git
每个 user story 完成时 git commit，标记为稳定状态，方便回退。

## 常见错误清单（Review 重点）

写代码或 review 时重点检查这几处：

1. ❌ 启用 Spring AI 的自动 tool 执行 → tool 被调两次。**必须禁用。**
2. ❌ ProviderService 靠扫描容器里所有 ChatModel 区分 Provider → 多 Provider 时歧义。**必须用显式 name 映射。**
3. ❌ 把 Tool 拆成 builtin/skill/mcp 多个模块 → **应是合并的 `oryxos-tool` 一个模块。**
4. ❌ 把 SKILL.md 当成一种 Tool → **它是 prompt 输入源，归 ContextLoader，跟 Bootstrap 同类。**
5. ❌ `tool_invocations` / `llm_calls` 只写日志不落库 → **核心阶段就要写库。**
6. ❌ Memory 简化成跟 Session 合并 → **应是 MemoryService 三层统一门面。**
7. ❌ 用了非 JDK 21 特性，或改了 ReAct 实现方式 → 违反宪章。

## 核心阶段不做的部分（不要实现，避免越界）

核心阶段交付运行时内核。以下都放**扩展/社区阶段**，不要在本阶段做：

- 多 Channel（企业微信、飞书、钉钉、Slack、邮件）
- Provider fallback / circuit breaker / hedge racing / Adaptive Routing
- Memory 自动抽取、语义检索（向量库）、情景记忆、Memory Wiki、压缩
- MCP Server 暴露（OryxOS 作为 MCP server）
- Tool Policy（allow/deny）、Tool LRU 加载、完整 Docker/K8s Sandbox 隔离
- Web Service 剩余 15 个端点、SSE 流式、WebSocket、认证/鉴权、RBAC、限流
- Web 仪表板、SSO、多租户、完整审计（查询/报表）、可观测性（Prometheus 指标）
- 集群化部署、高可用、企业 IT 系统 connector、SDK 多语言、Skills Marketplace、K8s Operator

## 五大验收 Demo（硬条件）

| Demo | 能力 | 内容 |
| --- | --- | --- |
| Demo 一 | 对接 LLM + ReAct | "查天气并写日报"，Agent 调天气 API、用文件 Tool 写日报到本地 |
| Demo 二 | Memory | 第一次对话告诉偏好（Spring Boot、K8s），Agent 调 save_memory；第二次对话能引用记忆回答 |
| Demo 三 | Plugin Tool + MCP | Agent 通过 MCP Client 调外部 server 的工具完成跨工具任务 |
| Demo 四 | Web Service 同步调用 | 外部系统创建 Session、发消息、获取响应、归档，链路跑通 |
| Demo 五 | Web Service 多端点联动 | 外部系统先后调 info、profiles、tools、invoke、memory 完成一次业务流程 |

## 参考

- 完整需求：`docs/DemandAnalysis.md`
- 完整技术方案：`docs/TechnicalSolution.md`
- 定位与愿景：`docs/IndustryResearch.md`
- AI 编程实施：`docs/AiProgrammingGuide.md`
