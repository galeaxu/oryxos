# Data Model: 核心阶段 Agent OS 运行时内核

**Feature**: `001-core-agent-os-kernel` | **Date**: 2026-10-27
**Contracts**: 见 [contracts/](./contracts/) | **DDL 权威来源**: plan.md §3.2

本文件描述领域实体、字段语义、关系与校验规则。DDL 伪代码在 plan.md §3.2，此处不重复。

---

## 1. 实体关系总览

```text
Agent 目录 (.oryxos/agents/<name>/)
  │  1
  │  ├── AGENT.md            (frontmatter → Profile；正文 → system prompt)
  │  └── skills/<name>       (相对软连接) ──► .oryxos/skills/<name>/SKILL.md
  │
  │  N
  ▼
Session (sessions 表)
  │  1
  ├───────── N ──► Message (嵌在 messages_json 内，非独立表)
  ├───────── N ──► ToolInvocation (tool_invocations 表)
  └───────── N ──► LlmCall (llm_calls 表)

Tool 定义 (非持久化，启动期装载)
  └── OryxTool { name, description, inputSchema, source }
```

**关键关系约束**

- `Session` 与 `ToolInvocation` / `LlmCall` 是 **1:N 且弱引用**（`session_id` 可为 NULL，用于无状态 `agents/{name}/invoke` 调用）。
- `Message` 不是独立表：整段历史序列化为 JSON 存在 `sessions.messages_json`（决策见 plan.md §3.2 设计说明）。
- 审计表**不做外键约束**：审计记录必须保留，不得因会话归档/删除而级联消失（宪法 V）。

---

## 2. 领域实体

### 2.1 Agent（目录实体，非数据库表）

**真相源**: `.oryxos/agents/<name>/AGENT.md`

| 字段 | 来源 | 类型 | 必填 | 校验规则 |
| --- | --- | --- | --- | --- |
| `name` | 目录名 | String | ✅ | 与目录名一致；`^[a-z0-9][a-z0-9_-]{0,63}$` |
| `description` | frontmatter | String | ✅ | 非空 |
| `identity.agentName` | frontmatter | String | ❌ | 默认取 `name` |
| `provider.name` | frontmatter | String | ✅ | 必须存在于 `ProviderRegistry.names()`（FR-008） |
| `provider.model` | frontmatter | String | ✅ | 必须在该 provider 的 `models` 集合内 |
| `provider.temperature` | frontmatter | Double | ❌ | `[0.0, 2.0]` |
| `tools` | frontmatter | List\<String\> | ❌ | 每项必须存在于全局池；未知项**报错**不静默忽略 |
| `mcpServers` | frontmatter | List\<String\> | ❌ | 每项必须存在于 `mcp_servers.yaml` |
| `settings.maxIterations` | frontmatter | int | ❌ | 默认 10，`>= 1` |
| `settings.maxHistoryTurns` | frontmatter | int | ❌ | 默认 20，`>= 1` |
| 正文 | AGENT.md body | Markdown | ✅ | 每轮现读，**不缓存**（宪法 IV） |

**状态/一致性检查（FR-034）**

| 状态 | 含义 | 处理 |
| --- | --- | --- |
| `valid` | 软连接相对、目标在 `.oryxos/skills/` 内、名称匹配 | 注入元数据 |
| `dangling` | 软连接目标不存在 | 启动校验报错 |
| `escaped` | 目标真实路径越出 `.oryxos/skills/` 或是多级链 | 拒绝，报安全错误 |
| `invalid-target` | 不是软连接（普通文件/绝对链接/目录副本） | 拒绝 |
| `name-mismatch` | `SKILL.md` frontmatter `name` ≠ 链接名 | 拒绝 |
| `stale-reference` | Agent 引用了已被删除的公共 Skill | 启动恢复时报错并回列 |

### 2.2 Skill（目录实体，非数据库表）

**真相源**: `.oryxos/skills/<name>/SKILL.md`

| 字段 | 来源 | 必填 | 校验规则 |
| --- | --- | --- | --- |
| `name` | frontmatter | ✅ | 与目录名一致 |
| `description` | frontmatter | ✅ | 非空；**唯一注入 Context 的内容**（渐进披露） |
| 正文/引用/脚本 | 文件内容 | ❌ | **不预载**，仅经 `read_file` / `shell` 按需读取 |

**删除规则（FR-034）**: 仍被任一 Agent 软连接引用时**默认拒绝删除**，并返回引用 Agent 列表。

### 2.3 Tool（内存实体，`OryxTool`）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `name` | String | 全局唯一；同 Agent 视图内冲突即报错（FR-030a） |
| `description` | String | 供 LLM 判断调用时机 |
| `inputSchema` | String(JSON) | JSON Schema，`ToolCallAdapter` 直接透传 |
| `source` | Enum | `BUILTIN` / `MCP` / `PLUGIN_ANNOTATION` / `AGENT_PRIVATE` |

**可见集合规则（FR-030）**: `visible(agent) = (全局池 ∩ profile.tools) ∪ agent私有工具`。

### 2.4 Session（`sessions` 表）

| 字段 | 类型 | 必填 | 校验规则 |
| --- | --- | --- | --- |
| `session_id` | String | ✅ | 主键；`channel + user_id + agent` 派生或 UUID |
| `profile_name` | String | ✅ | 必须对应已存在的 Agent 目录（FR-039 未知 Agent → 404） |
| `channel` | String | ✅ | 默认 `"cli"`；本期仅 `cli` / `http` |
| `user_id` | String | ❌ | Channel 提供 |
| `messages_json` | TEXT | ✅ | JSON 数组；单条消息 ≤ 32KB（FR-038） |
| `status` | Enum | ✅ | `ACTIVE` / `ARCHIVED` |
| `created_at` | Instant | ✅ | 创建后不变 |
| `last_active_at` | Instant | ✅ | 每次 append 更新 |
| `archived_at` | Instant | ❌ | 归档时写入 |
| `version` | int | ✅ | 乐观锁；并发 append 冲突时重试一次后报错 |

**状态转换**

```text
[新建] --create--> ACTIVE
ACTIVE --append message--> ACTIVE  (last_active_at 更新)
ACTIVE --DELETE /api/v1/sessions/{id}--> ARCHIVED (archived_at 写入)
ARCHIVED --(不再接受 append)--> 终态
```

**不变量**

- `ARCHIVED` 会话上的 `POST /messages` 必须返回 4xx，不得隐式复活。
- `messages_json` 必须始终保持合法 JSON（写前序列化校验）。

### 2.5 Message（`ChatMessage` 记录，嵌于 `session.messages_json`）

| 字段 | 类型 | 校验规则 |
| --- | --- | --- |
| `role` | String | `system` / `user` / `assistant` / `tool` |
| `content` | String | `user` 消息 ≤ 32KB |
| `timestamp` | Instant | ISO-8601 |
| `toolCallId` | String? | **仅 `tool` 角色**必填，与发起调用的 `ToolCall.callId` 对齐 |
| `toolCalls` | List\<ToolCall\>? | **仅 `assistant` 角色**可携带；写入即代表本轮要求调工具 |

> 注：现有骨架 `ChatMessage` 为三字段 record，实现阶段需扩展 `toolCallId` / `toolCalls`（D-03 的配套变更）。

### 2.6 ToolCall（值对象，LLM 响应中返回）

| 字段 | 类型 | 校验规则 |
| --- | --- | --- |
| `callId` | String | 非空；回填时原样透传（OpenAI 协议要求） |
| `toolName` | String | 必须在该 Agent 可见集合内，否则拒绝执行（FR-030） |
| `argumentsJson` | String | 必须是合法 JSON；解析失败 → `ToolResult.fail(..., retryable=true)` |

### 2.7 ToolInvocation（`tool_invocations` 表）

| 字段 | 类型 | 必填 | 校验规则 |
| --- | --- | --- | --- |
| `invocation_id` | String | ✅ | UUID |
| `session_id` | String | ❌ | 无状态调用为 NULL |
| `profile_name` | String | ✅ | Agent 目录名 |
| `tool_name` | String | ✅ | — |
| `tool_source` | Enum | ✅ | 见 2.3 |
| `parameters` | TEXT | ❌ | 原始入参 JSON，**凭证字段脱敏** |
| `status` | Enum | ✅ | `SUCCESS` / `FAILED` / `DENIED` |
| `result` | TEXT | ❌ | 成功内容；> 64KB 截断并标记 |
| `error` | TEXT | ❌ | 失败原因；沙箱拒绝时写白名单不符详情 |
| `started_at` / `completed_at` | Instant | ✅ / ❌ | — |
| `duration_ms` | long | ❌ | `completed - started` |

**不变量**

- 每次 `execute` **恰好一条**记录，且在校验失败（DENIED）时也必须存在（SC-008）。
- `status=DENIED` ⇒ `error` 非空 且 `result` 为空。

### 2.8 LlmCall（`llm_calls` 表）

| 字段 | 类型 | 必填 | 校验规则 |
| --- | --- | --- | --- |
| `call_id` | String | ✅ | UUID |
| `session_id` | String | ❌ | 无状态调用为 NULL |
| `profile_name` | String | ✅ | — |
| `provider` | String | ✅ | `ProviderRegistry` 的 key |
| `model` | String | ✅ | 实际请求的模型标识 |
| `prompt_tokens` / `completion_tokens` / `total_tokens` | Integer | ❌ | 厂商不返回时为 NULL（不臆造 0） |
| `latency_ms` | long | ❌ | 调用墙钟耗时 |
| `status` | Enum | ✅ | `SUCCESS` / `FAILED` |
| `error` | TEXT | ❌ | 调用失败原因 |
| `iteration` | int | ✅ | 当前 ReAct 迭代序号，从 0 开始（FR-043 可还原链路） |
| `started_at` / `completed_at` | Instant | ✅ / ❌ | — |

**不变量**

- 一次 `ProviderService.call` 恰好一条记录；无论成功失败（宪法 V）。
- 同一 `session_id` 下 `iteration` 单调递增，用于还原「思考-调用-回填」链路。

---

## 3. 时间与类型约定

| 约定 | 规则 | 理由 |
| --- | --- | --- |
| 时间存储 | `TEXT` ISO-8601 UTC（`Instant.toString()`） | SQLite 无原生时间类型，community dialect 时区处理不一致 |
| JPA 映射 | `Instant` + 自定义 `AttributeConverter` | 保证写入/读取对称 |
| 枚举存储 | 大写字符串（`SUCCESS`/`DENIED`/`ACTIVE`） | 可读、可 SQL 过滤；避免 ordinal 漂移 |
| 大文本 | `TEXT`，单字段超 64KB 截断 + `...[truncated]` | 防单条审计撑爆库 |

---

## 4. 校验规则汇总（映射 FR）

| 规则 | 触发点 | 失败行为 | FR |
| --- | --- | --- | --- |
| Agent 名 = 目录名 | `AgentLoader` | 启动报错 | FR-001 |
| provider 必须已注册 | `ProviderRegistry.require` | 明确报错含已注册列表 | FR-008 |
| model 必须受支持 | `DefaultProviderService` | 明确报错 | FR-008 |
| 工具名冲突 | `AgentToolView.resolve` | 抛异常，不覆盖 | FR-030a |
| 私有工具定义非法 | `AgentLoader` | 报错并指出 Agent | FR-030c |
| 工具不在可见集合 | `ToolExecutor` | `DENIED` + 审计 | FR-030 |
| 路径越界 | `SandboxChecker` | `DENIED` + 审计 | FR-045/046 |
| 命令含结构符 | `SandboxChecker` | `DENIED` + 审计 | FR-045 |
| 域名不在白名单 | `SandboxChecker` | `DENIED` + 审计 | FR-047 |
| 消息 > 32KB | Controller / `SessionManager` | 400 | FR-038 |
| ARCHIVED 后 append | `SessionManager` | 4xx | FR-041 |
