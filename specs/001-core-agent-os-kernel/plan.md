# Implementation Plan: 核心阶段 Agent OS 运行时内核（五大核心能力）

**Branch**: `001-core-agent-os-kernel` | **Date**: 2026-10-27 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-core-agent-os-kernel/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

交付 Agent OS 的**运行时内核**最小完备集：业务方用一个目录定义一个 Agent，系统自行掌控 ReAct 循环，调用多家 LLM 与工具完成任务，会话与长期记忆可跨重启恢复，并通过 REST 接口被外部系统调用。

技术路径：Spring Boot 3.3.5 单体多模块（现有 9 个模块，**不新增模块**），Spring MVC + 虚拟线程全程同步阻塞；Spring AI/Alibaba **只做协议转换与 JSON Schema 生成**，工具调度与执行由自有 `ReActLoop` + `ToolExecutor` 独占；Provider 采用**显式 name → ChatModel 映射**；工具统一收敛到 `OryxTool` 抽象并经 `SandboxChecker` 白名单 + 真实路径校验；`tool_invocations` / `llm_calls` 审计 Day One 落库；表结构由 **Flyway** 管理。

## Technical Context

<!--
  ACTION REQUIRED: Replace the content in this section with the technical details
  for the project. The structure here is presented in advisory capacity to guide
  the iteration process.
-->

**Language/Version**: Java 21（虚拟线程，`spring.threads.virtual.enabled=true`），Spring Boot 3.3.5
**Primary Dependencies**: Spring Boot Starter / Web / Data JPA；Spring AI + Spring AI Alibaba（仅协议层，**禁用自动装配与自动 tool 执行**）；Picocli 4.7.6；SnakeYAML 2.3；SQLite JDBC 3.46.1.3；hibernate-community-dialects 6.5.3；MCP Java SDK（stdio transport 优先）；Jackson（复用 Spring Boot 传递依赖，不新增坐标）；Flyway（新增依赖，理由见 Complexity Tracking）
**Storage**: SQLite（默认，`.oryxos/oryxos.db`）/ PostgreSQL（部署选项）；表结构由 Flyway 迁移脚本管理，`ddl-auto` 改为 `validate`（当前为 `update`，违反宪法 VIII）
**Testing**: JUnit 5 + Spring Boot Test + AssertJ；契约测试用 MockMvc；沙箱与真实路径用例用 `@TempDir`；Provider 用可替换的 Fake ChatModel
**Target Platform**: 企业自有 Linux K8s / VM / 物理机，单 fat JAR
**Project Type**: 单体多模块 Web 服务 + CLI
**Performance Goals**: 会话创建 p95 < 200ms；单实例 ≥ 10 Agent 并存、≥ 100 并发会话（SC-009）；Agent 调用 60s 超时 → 504；单条消息 ≤ 32KB
**Constraints**: 全程同步阻塞，**不得引入** Reactor / WebFlux / `CompletableFuture` / SSE；不得使用 `SecurityManager`；`llm_calls`/`tool_invocations` 写入不可省；审计查询接口不在本期
**Scale/Scope**: 10 个 REST 端点、5 个内置 Tool、12 个 CLI 子命令、3 张核心表、9 个 Maven 模块（0 新增）

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| # | 原则 | 门禁项 | 结论 |
| --- | --- | --- | --- |
| I | 自实现 ReAct 循环 (NON-NEGOTIABLE) | 循环由 `ReActLoop` 独占；迭代上限/终止/裁剪可配置；FR-011~016 全部落在 core | ✅ PASS |
| II | Spring AI 仅协议转换与 Schema 生成 (NON-NEGOTIABLE) | 仅 `chatModel.call(new Prompt(...))`；禁用 `ChatClient` 自动 tool 执行与 `*AutoConfiguration` | ✅ PASS（须显式排除自动装配） |
| III | Provider 显式映射 | `Map<String, ChatModel>` 显式构建；未注册名称报错（FR-008）；不扫描 Bean 类型 | ✅ PASS |
| IV | 一个目录 = 一个 Agent；Skill 本地软连接 + 渐进披露 | `AgentLoader.deriveProfile` 读 `AGENT.md`；Skill 仅软连接绑定；**Skill 不进 ToolRegistry**；不新增 `use_skill` | ✅ PASS |
| V | 审计 Day One 落库 (NON-NEGOTIABLE) | 两表写入随调用链路同步落库（FR-042/043） | ✅ PASS |
| VI | 沙箱 + 真实路径，不用 SecurityManager (NON-NEGOTIABLE) | `SandboxChecker` 真实路径校验；拒绝绝对/链式/越界软连接 | ✅ PASS（现有骨架仅字符串比较，**必须重写**） |
| VII | 同步执行 + 虚拟线程 | 所有 API/循环同步；无 Reactor/WebFlux/CompletableFuture/SSE | ✅ PASS |
| VIII | 目录配置即 Agent，实例无状态、状态外置；迁移脚本管理 | 状态全在 SQLite + 文件；**Flyway 取代 `ddl-auto: update`** | ⚠️ CONDITIONAL（当前 `ddl-auto=update` 违反，修正后达标） |

**模块约束**：宪法允许按需演进模块，但本特性**不新增模块**，全部落在既有 9 个模块内；跨模块契约放 `oryxos-core`（依赖倒置）。无模块间循环依赖。

**GATE 结论**：PASS（VIII 的 CONDITIONAL 项由既有配置缺陷导致，修正动作已列为 P0 任务，不计为本特性新增复杂度）。

## Project Structure

### Documentation (this feature)

```text
specs/001-core-agent-os-kernel/
├── plan.md              # 本文件
├── spec.md              # 特性规格
├── research.md          # Phase 0：技术决策与未知项消解
├── data-model.md        # Phase 1：实体、关系、校验规则
├── quickstart.md        # Phase 1：端到端验证指南
├── checklists/
│   └── requirements.md  # 规格质量检查清单
├── contracts/           # Phase 1：接口契约
│   ├── rest-api.md
│   ├── cli.md
│   └── workspace-layout.md
└── tasks.md             # Phase 2（由 /speckit-tasks 生成）
```

### Source Code (repository root)
<!--
  ACTION REQUIRED: Replace the placeholder tree below with the concrete layout
  for this feature. Delete unused options and expand the chosen structure with
  real paths (e.g., apps/admin, packages/something). The delivered plan must
  not include Option labels.
-->

现有 9 个模块，本特性在各模块内补齐骨架中的 `TODO`。**新增文件**以 `+` 标注。

```text
oryxos-core/                                # 引擎与契约（不依赖任何 OryxOS 内部模块）
└── src/main/java/com/oryxos/core/
    ├── agent/AgentService.java             # 已有接口
    ├── profile/Profile.java                # 已有
    ├── provider/ProviderService.java       # 已有接口（签名需扩展，见 D-03）
    ├── react/ReActLoop.java                # 已有骨架 → 实现完整循环
    ├── react/ToolExecutor.java             # 已有接口
    ├── + react/PromptBuilder.java          # system prompt + 历史 + 工具清单
    ├── + react/LoopResult.java             # 循环结果（reply / iterations / terminated）
    ├── + agent/AgentLoader.java            # 扫描 .oryxos/agents/*/AGENT.md → Profile
    ├── + agent/ContextLoader.java          # AGENT.md 正文 + Skill 元数据（每轮现读）
    ├── + agent/SkillBinder.java            # 软连接绑定与一致性检查
    ├── + agent/WorkspaceInitializer.java   # oryxos init 的目录骨架生成
    └── + config/ConfigLoader.java          # 必填项校验、${ENV_VAR} 解析

oryxos-provider/                            # 能力一
└── src/main/java/com/oryxos/provider/
    ├── DefaultProviderService.java         # 已有骨架 → 显式映射 + 调用
    ├── + ProviderRegistry.java             # 显式 name → ChatModel 映射表
    ├── + ToolCallAdapter.java              # OryxTool ↔ Spring AI ToolCallback（仅 schema）
    └── + ProviderException.java            # 未注册 provider / 调用失败

oryxos-memory/                              # 能力三
└── src/main/java/com/oryxos/memory/
    ├── DefaultMemoryService.java           # 已有
    ├── LongTermMemory.java                 # 已有（recallByKeyword / truncate）
    └── + MemoryTools.java                  # save_memory / recall_memory

oryxos-tool/                                # 能力四（内置 Tool + MCP + Registry + Sandbox 合并在一个模块）
└── src/main/java/com/oryxos/tool/
    ├── builtin/ReadFileTool.java           # 已有（JSON 解析待补）
    ├── + builtin/WriteFileTool.java
    ├── + builtin/ListDirTool.java
    ├── + builtin/ShellTool.java
    ├── + builtin/HttpTools.java            # http_get / http_post
    ├── registry/ToolRegistry.java          # 已有 → 支持来源与冲突检测
    ├── + registry/AgentToolView.java       # 全局池子集 + Agent 私有工具（FR-030）
    ├── sandbox/SandboxChecker.java         # 已有骨架 → **重写真实路径校验**
    ├── sandbox/SandboxViolationException.java
    └── + mcp/McpClientService.java / McpToolAdapter.java

oryxos-storage/                             # 支撑：SQLite 三表 + Flyway
└── src/main/
    ├── java/com/oryxos/storage/
    │   ├── entity/{SessionEntity,ToolInvocationEntity,LlmCallEntity}.java   # 已有
    │   ├── repository/{...}Repository.java                                  # 已有
    │   ├── + session/SessionManager.java    # 会话载入/持久化/归档
    │   └── + audit/AuditRecorder.java       # 两表写入（Day One）
    └── resources/db/migration/
        └── + V1__init_core_tables.sql

oryxos-web/                                 # 能力五：10 个端点（6 个 Controller 已有骨架）
oryxos-channel-cli/                         # CLI Channel
oryxos-cli/                                 # Picocli 12 子命令（已有骨架）
oryxos-boot/                                # 装配 + fat JAR
```

**Structure Decision**: 沿用既有 Maven 多模块单体结构（`oryxos-core` 承载全部跨模块契约，下游模块实现）。不新建模块——`SandboxChecker` 留在 `oryxos-tool`（宪法 IV 末句明确「内置 Tool 与 MCP Client 仍合并在单一 `oryxos-tool` 模块」）。

## 1. 模块设计（严格映射既有 9 模块）

### 1.1 依赖方向

```text
oryxos-boot  ──► cli, channel-cli, web, tool, memory, provider, storage ──► core
```

规则：`core` 不依赖任何内部模块；能力模块之间**不得**相互依赖（`tool` 不得依赖 `memory`，`MemoryTools` 由 boot 装配时注册进 `ToolRegistry`）；下游模块只实现 `core` 的接口。

### 1.2 各模块职责与关键类

| 模块 | 承担的 FR | 关键类 | 禁止事项 |
| --- | --- | --- | --- |
| **oryxos-core** | FR-001~005、011~016、025、031~035、049 | `ReActLoop`、`PromptBuilder`、`ToolExecutor`、`AgentLoader`、`ContextLoader`、`SkillBinder`、`WorkspaceInitializer`、`ConfigLoader`、`OryxTool`/`ToolResult` | 不得引入 Spring AI、JDBC、HTTP 依赖 |
| **oryxos-provider** | FR-006~010、043 | `ProviderRegistry`、`DefaultProviderService`、`ToolCallAdapter` | 不得自行驱动工具执行；不得按 Bean 类型扫描 |
| **oryxos-memory** | FR-018~022、024 | `DefaultMemoryService`、`LongTermMemory`、`MemoryTools` | 不得接管会话持久化（归 storage） |
| **oryxos-tool** | FR-026~030、042、045~048 | `ToolRegistry`、`AgentToolView`、5 个内置 Tool、`SandboxChecker`、`McpClientService` | 不得承载 Skill 加载（归 core）；不得拆分子模块 |
| **oryxos-storage** | FR-017、042~044、053 | `SessionManager`、`AuditRecorder`、3 个 Entity/Repository、Flyway 脚本 | 不得反向依赖能力模块 |
| **oryxos-web** | FR-036~041 | 6 个 Controller、`GlobalExceptionHandler` | Controller 只做参数校验/包装/错误映射，不含业务逻辑 |
| **oryxos-channel-cli** | FR-049 | `CliChannel` | — |
| **oryxos-cli** | FR-050 | `OryxOsCommand` 12 子命令 | — |
| **oryxos-boot** | FR-049、051、052 | `OryxOsApplication`、`OryxOsConfiguration` | 不得让 `serve/chat` 之外的命令拉起 Spring |

### 1.3 装配点

`oryxos-boot` 新增 `OryxOsConfiguration`（`@Configuration`），负责构造链：`ProviderRegistry` → `SandboxChecker`（读白名单）→ 内置 Tool → `ToolRegistry` → `SessionManager`/`AuditRecorder` → `ToolExecutor` → `ReActLoop` → `AgentService`。**这是唯一装配点**，避免 `@Component` 散落导致循环依赖。

## 2. 接口契约

### 2.1 `OryxTool`（`oryxos-core`，已有，本特性**不改签名**）

```java
package com.oryxos.core.tool;

public interface OryxTool {
    String getName();                        // 全局唯一工具名（跨来源冲突即启动失败）
    String getDescription();                 // 供 LLM 判断何时调用
    String getInputSchema();                 // JSON Schema（Function Calling 用）
    ToolResult execute(String jsonInput);    // 同步阻塞，禁止抛受检异常
}

public record ToolResult(boolean success, String content, String error, boolean retryable) {
    public static ToolResult ok(String content) { ... }
    public static ToolResult fail(String error, boolean retryable) { ... }
}
```

**契约要点**

1. `execute` **同步、无异常逃逸**：所有失败折叠为 `ToolResult.fail(error, retryable)`；沙箱拒绝同样走 `fail(msg, false)`（不可重试）。
2. `retryable` 由工具自身语义决定（`read_file` IO 失败可重试、`shell` 白名单拒绝不可重试），驱动 FR-015。
3. `jsonInput` 由 `ToolExecutor` 统一审计落库：**参数原文**进 `tool_invocations.parameters`。
4. 不提供 `getSource()` 等来源方法——来源由 `ToolRegistry` 侧表维护，保持 `OryxTool` 对循环透明（FR-025）。

### 2.2 `ProviderService` 与显式映射数据结构

现有骨架签名 `String call(Profile, String prompt)` **不足以承载工具调用**（FR-011~013 需要 tool_calls 与 tool_call_id）。按决策 D-03，本特性**直接改签名**（不保留 deprecated 重载）：

```java
package com.oryxos.core.provider;

public interface ProviderService {
    /** 一次 LLM 调用。返回结构化响应（含 tool calls 与用量），供 ReActLoop 自己决策。 */
    ChatResponse call(Profile profile, Prompt prompt);

    /** 一次调用的用量信息（供 llm_calls 审计与 FR-009）。 */
    record InvocationInfo(String provider, String model,
                          Integer promptTokens, Integer completionTokens,
                          Integer totalTokens, long latencyMs) {}
}

public record ChatResponse(String content, List<ToolCall> toolCalls, InvocationInfo info) {}
public record ToolCall(String callId, String toolName, String argumentsJson) {}
```

**显式映射表（Provider 契约核心）**

```java
package com.oryxos.provider;

/** 显式映射条目：一个 provider name 的全部信息。 */
public record ProviderEntry(
        String name,           // 唯一名称，如 "deepseek" / "qwen" / "kimi"
        ChatModel chatModel,   // Spring AI ChatModel 实例（仅做协议转换）
        String baseUrl,
        Set<String> models) {} // 支持的模型标识（Profile 引用时校验）

/** 显式映射表：name → entry。构建期冻结，运行期只读，不做类型扫描。 */
public final class ProviderRegistry {
    private final Map<String, ProviderEntry> byName;   // LinkedHashMap，构建后 unmodifiable

    public ProviderRegistry(List<ProviderEntry> entries) { /* 重名即抛启动异常 */ }

    public ProviderEntry require(String name) {
        ProviderEntry e = byName.get(name);
        if (e == null) {
            throw new ProviderNotRegisteredException(name, byName.keySet()); // FR-008 / FR-010
        }
        return e;
    }
    public Set<String> names() { return byName.keySet(); }
}
```

**关键约束**

- `byName` 仅由显式配置（`application.yaml` 的 `oryxos.providers[*]`）构建，**禁止** `context.getBeansOfType(ChatModel.class)`（宪法 III / CLAUDE.md 错误清单 #2）。
- `ProviderNotRegisteredException` 消息必须含 Agent 名、字段名、已注册名称集合（FR-005 / FR-008）。
- 不做 fallback、不做 hedge racing（FR-010）：`require` 失败直接抛出，由 `GlobalExceptionHandler` 映射为明确错误。

### 2.3 `ToolExecutor` 与可见工具集合（FR-030）

```java
public interface ToolExecutor {
    ToolResult execute(ToolInvocation invocation);   // 可见性校验 → 沙箱 → 执行 → 审计
}

/** 全局池子集 + Agent 私有工具合并后的视图（FR-030 / FR-030a）。 */
public final class AgentToolView {
    public static AgentToolView resolve(Profile profile, ToolRegistry globalPool);
    public Optional<OryxTool> find(String name);
    public List<OryxTool> visible();     // 供 PromptBuilder 渲染工具清单
}
```

装配期由 `AgentToolView.resolve` 合并，**名称冲突直接抛异常**（FR-030a）。运行期 `ToolExecutor` 从视图 `find`，找不到即 `fail("工具不可用或被拒绝", false)`（FR-030）。

### 2.4 `SandboxChecker`

```java
public class SandboxChecker {
    public Path checkFilePath(String path);                   // 真实路径校验，返回 realPath
    public Path checkFilePathForCreate(String path);          // 新建：最近存在父目录
    public void checkShellCommand(String command);
    public void checkHttpUrl(String url);
    public Path checkSkillLink(Path agentDir, Path link);     // FR-034 / 宪法 VI
    // 全部失败抛 SandboxViolationException（RuntimeException）
}
```

## 3. 存储设计

### 3.1 迁移管理（FR-053）

- 引入 Flyway（`oryxos-storage`），脚本目录 `classpath:db/migration`。
- `spring.jpa.hibernate.ddl-auto`：`update` → **`validate`**（Flyway 建表后 Hibernate 只校验）。
- `spring.flyway.enabled=true`，`baseline-on-migrate=true`（兼容已存在的 `oryxos.db`）。
- SQLite 限制：**不支持 `ALTER COLUMN`**，后续演进只能「新建表 + 数据搬迁」；本特性首次建表一次成型。

### 3.2 核心 DDL（`V1__init_core_tables.sql`）

```sql
-- ============ sessions：会话元数据 + JSON 序列化的对话历史 ============
CREATE TABLE sessions (
    session_id      TEXT    PRIMARY KEY,                    -- channel+user+agent 派生或 UUID
    profile_name    TEXT    NOT NULL,                       -- 所属 Agent 目录名
    channel         TEXT    NOT NULL DEFAULT 'cli',         -- cli / http / ...
    user_id         TEXT,
    messages_json   TEXT    NOT NULL DEFAULT '[]',          -- List<ChatMessage> 的 JSON
    status          TEXT    NOT NULL DEFAULT 'ACTIVE',      -- ACTIVE / ARCHIVED
    created_at      TEXT    NOT NULL,                       -- ISO-8601 UTC
    last_active_at  TEXT    NOT NULL,
    archived_at     TEXT,
    version         INTEGER NOT NULL DEFAULT 0              -- 乐观锁（并发同会话写入）
);
CREATE INDEX idx_sessions_status_active ON sessions(status, last_active_at DESC);
CREATE INDEX idx_sessions_profile       ON sessions(profile_name, last_active_at DESC);

-- ============ tool_invocations：工具调用审计（Day One，宪法 V）============
CREATE TABLE tool_invocations (
    invocation_id   TEXT    PRIMARY KEY,                    -- UUID
    session_id      TEXT,                                   -- 无状态调用时为 NULL
    profile_name    TEXT    NOT NULL,                       -- Agent 目录名
    tool_name       TEXT    NOT NULL,
    tool_source     TEXT    NOT NULL,                       -- BUILTIN / MCP / PLUGIN_ANNOTATION / AGENT_PRIVATE
    parameters      TEXT,                                   -- 原始入参 JSON（脱敏后）
    status          TEXT    NOT NULL,                       -- SUCCESS / FAILED / DENIED
    result          TEXT,                                   -- 成功结果（超长截断）
    error           TEXT,                                   -- 失败原因
    started_at      TEXT    NOT NULL,
    completed_at    TEXT,
    duration_ms     INTEGER
);
CREATE INDEX idx_tool_inv_session ON tool_invocations(session_id, started_at);
CREATE INDEX idx_tool_inv_tool    ON tool_invocations(tool_name, started_at);

-- ============ llm_calls：模型调用审计（Day One，宪法 V）============
CREATE TABLE llm_calls (
    call_id           TEXT    PRIMARY KEY,                  -- UUID
    session_id        TEXT,                                 -- 无状态调用时为 NULL
    profile_name      TEXT    NOT NULL,
    provider          TEXT    NOT NULL,                     -- provider name（显式映射键）
    model             TEXT    NOT NULL,
    prompt_tokens     INTEGER,
    completion_tokens INTEGER,
    total_tokens      INTEGER,
    latency_ms        INTEGER,
    status            TEXT    NOT NULL,                     -- SUCCESS / FAILED
    error             TEXT,
    iteration         INTEGER NOT NULL DEFAULT 0,           -- 第几轮 ReAct 迭代
    started_at        TEXT    NOT NULL,
    completed_at      TEXT
);
CREATE INDEX idx_llm_calls_session ON llm_calls(session_id, started_at);
```

**设计说明**

- **时间统一 `TEXT` ISO-8601 UTC**：SQLite 无原生时间类型；JPA `Instant` + `AttributeConverter` 统一序列化，避免 community dialect 时区歧义。
- **`messages_json` 整列存历史**（而非 `messages` 子表）：符合技术方案 §9.2 与现有 `SessionEntity`；`version` 列用于并发追加的乐观锁。
- **`result`/`error` 截断**：单字段超 64KB 时截断并追加 `...[truncated]`，防单条审计撑爆库（FR-042 的完整性以「可还原调用要素」为准）。
- **不建 `messages` 表、不建向量表**：核心阶段明确排除（FR-018、Assumptions）。
- **审计写入与业务同步**：`AuditRecorder.recordTool(...)` 在 `ToolExecutor` 内同步调用，确保 FR-042/043 不可省。

## 4. 安全防御：`SandboxChecker` 执行时序

### 4.1 总体调用链（File 场景）

```text
ReActLoop
  └─ ToolExecutor.execute(invocation)
       ├─ ① AgentToolView.find(toolName)          → 不在可见集合 → DENIED（FR-030）
       ├─ ② OryxTool.execute(jsonInput)
       │     └─ ReadFileTool.execute
       │          ├─ ③ SandboxChecker.checkFilePath(path)     ★ 安全门禁
       │          └─ ④ Files.readString(realPath)             ★ 必须用解析后的路径
       ├─ ⑤ AuditRecorder.recordTool(...)         → 无论成败都写（FR-042）
       └─ ⑥ 返回 ToolResult
```

**时序红线（File）**

1. **校验先于任何文件系统读操作**；不得先 `Files.exists()` 再看白名单（避免 TOCTOU 前置探针）。
2. **校验结果与执行对象必须是同一个 `Path`**：`checkFilePath` 返回的 `realPath` 才是 ④ 的输入，**不得**在 ④ 重新 `Path.of(jsonInput)`（否则存在符号链接替换窗口）。
3. 校验失败抛 `SandboxViolationException` → `ToolExecutor` 捕获转为 `ToolResult.fail(msg, false)` 并落 `status=DENIED`，**不部分执行**（SC-008）。

### 4.2 `checkFilePath` 判定步骤（FR-045 / FR-046 / 宪法 VI）

```text
输入: rawPath（来自 LLM 的工具参数，不可信）

1. 拒绝空值 / 含 NUL 字符
2. p = Path.of(rawPath).toAbsolutePath().normalize()      // 仅拼接归一，不代表安全
3. if (Files.exists(p, LinkOption.NOFOLLOW_LINKS)) {
       real = p.toRealPath()                              // 解析全部软连接 → 真实路径
   } else {
       ancestor = p 向上找到最近存在的父目录（NOFOLLOW）   // 创建/读不存在文件场景
       real = ancestor.toRealPath().resolve(p 相对 ancestor 的剩余段).normalize()
   }
4. for (root : allowedRoots) {
       rootReal = root.toRealPath()                       // 白名单根也解析真实路径
       if (real.startsWith(rootReal)) return real;        // ✅ 放行，返回真实路径供执行
   }
5. throw SandboxViolationException("路径越界: " + rawPath + ", 解析为 " + real)
```

**为什么必须 `toRealPath()`**：`..` 与软连接都能让 `normalize()+startsWith()` 得出「在根内」的错误结论。例：`.oryxos/agents/a/skills/x → /etc`，字符串仍以 `.oryxos/` 开头，真实路径却在根外。

**Windows 注意**：`toRealPath` 会解析盘符与大小写；白名单根同样解析，避免大小写差异误判。

### 4.3 Shell 场景时序（FR-045）

```text
1. 空命令/空白 → 拒绝
2. 剥离前置空白；若首字符为引号 → 拒绝（引号包裹的首 token 无法可靠识别）
3. 按空白切分取首个 token
4. 拒绝含结构符的命令：| ; & && || > >> < $( `   （核心阶段不给 LLM 组合权）
5. 归一化首 token（basename）与白名单比对
6. 通过 → ProcessBuilder 执行，带超时
```

**红线**：首 token 白名单**不能**挡住 `git; rm -rf /`。第 4 步的结构符拒绝是**必需**的，不能省——这是骨架版 `checkShellCommand` 的已知缺口，本特性必须补上。

### 4.4 HTTP 与 Skill 软连接

- `checkHttpUrl`：解析 `URI` → host 非空 → 通配白名单（`*.example.com` 需 `endsWith(".example.com")` **且** host 不等于裸后缀）；拒绝 `http://user@evil.com`、拒绝非 http/https scheme、拒绝字面 IP（除非白名单显式含该 IP）。
- `checkSkillLink`（`SkillBinder` 调用）：① 必须是**相对**软连接；② 目标必须在 `.oryxos/skills/` 真实路径内；③ 不解引用多级链（链式视为 `escaped`）；④ `SKILL.md` 的 `name` 必须与链接名一致，否则 `name-mismatch`。四类失败分别对应 FR-034 的 `dangling / escaped / invalid-target / name-mismatch`。

## 5. 实施顺序（对齐 US 依赖）

| 阶段 | 模块 | 交付 | 验收 |
| --- | --- | --- | --- |
| P0 地基 | storage, boot | Flyway 脚本、`ddl-auto=validate`、装配类 | 启动即建表，`mvn verify` 绿 |
| P1 / US-1 | core, provider | `ProviderRegistry`、`ConfigLoader`、`WorkspaceInitializer`、`AGENT.md` 加载 | SC-001 |
| P2 / US-2 | core, tool | `ReActLoop` 完整循环、`PromptBuilder`、`ToolExecutor`、`SandboxChecker` 重写、审计 | SC-002、SC-008 |
| P3 / US-3 | memory, storage | `SessionManager`、`MemoryTools`、跨重启恢复 | SC-003、SC-006 |
| P3 / US-4（并行） | tool, core | 5 内置 Tool、`AgentToolView`、MCP Client、`SkillBinder` | SC-004 |
| P4 / US-5 | web, cli | 10 端点、12 子命令、`GlobalExceptionHandler` | SC-005、SC-009 |

## 6. 风险与对策

| 风险 | 影响 | 对策 |
| --- | --- | --- |
| Spring AI 自动 tool 执行未禁用 → 工具被调两次 | 高 | 排除 `*AutoConfiguration`；`ToolCallAdapter` 只生成 schema，执行权在 `ReActLoop`；单测断言「一次 tool call 只产生一条 `tool_invocations`」 |
| SQLite 并发写锁 | 中 | 单写连接 + `busy_timeout`；会话追加用乐观锁 `version` |
| MCP Java SDK 成熟度 | 中 | 只做 stdio transport；连不通时该工具标记 `UNAVAILABLE`，不影响其他工具 |
| 上下文超限 | 中 | `PromptBuilder` 按 `max_history_turns`（默认 20）裁剪（FR-023），策略可配置 |
| Windows/Linux 路径差异 | 低 | 白名单根与目标都走 `toRealPath()`；测试双平台覆盖 |

## Complexity Tracking

> 无宪法违规项。下表记录**有意为之**的复杂度取舍。

| 取舍 | 为什么需要 | 更简单的方案为何不够 |
| --- | --- | --- |
| `ProviderService` 签名从 `String` 扩展为 `ChatResponse`（决策 D-03，直接改签名） | ReAct 循环必须自己拿到 `toolCalls` 才能自主推进迭代（原则 I） | 只返回字符串则循环无从判断是否需要调工具，等于把控制权交回框架 |
| `AgentToolView` 引入「全局池 + 私有」合并层 | 澄清结论 C，需在装配期做冲突检测 | 运行期直接查 `ToolRegistry` 则私有工具会泄漏给其他 Agent，违反 FR-030 |
| 引入 Flyway 依赖（决策 D-07） | 宪法 VIII 明确禁止依赖自动建表推导（FR-053） | 继续 `ddl-auto=update` 在 SQLite 上无法演进表结构 |
| 审计与调用同步写入 | 宪法 V「Day One 落库不可省」 | 异步/事后补写会丢记录，且与原则 VII 冲突 |
