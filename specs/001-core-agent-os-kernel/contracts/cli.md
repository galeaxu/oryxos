# CLI Contract: 核心阶段 Agent OS

**Feature**: `001-core-agent-os-kernel` | **Date**: 2026-10-27
**Binding**: `oryxos-cli` (Picocli 4.7.6) + `oryxos-channel-cli` | **FR**: FR-049 / FR-050

## 通用约定

| 项 | 规则 |
| --- | --- |
| 安装形态 | 单 fat JAR，`java -jar oryxos.jar <cmd>`；`oryxos` 为脚本别名 |
| 退出码 | `0` 成功 / `1` 业务错误 / `2` 参数错误（Picocli 默认） |
| 输出 | 人类可读默认；`--json` 输出机器可读（便于脚本/CI） |
| 工作目录 | `--workspace <path>`，默认 `./.oryxos`；亦可用 `ORYXOS_WORKSPACE` |
| 启动策略 | 无参数 / `serve` / `gateway` → 启动 Spring；其余 → 仅 Picocli，**不拉起 Spring** |

## 12 个子命令

| # | 命令 | 作用 | 对应端点 / 模块 |
| --- | --- | --- | --- |
| 1 | `oryxos init` | 初始化 `.oryxos/` 工作区骨架 | `WorkspaceInitializer` |
| 2 | `oryxos status` | 展示配置与运行状态 | `GET /info` `GET /health` |
| 3 | `oryxos chat` | 交互式 / 单条对话 | `CliChannel` → `AgentService` |
| 4 | `oryxos serve` | 启动 HTTP API 服务 | Spring Boot |
| 5 | `oryxos gateway` | 启动多渠道守护进程 | 本期等价 `serve`（多 Channel 属扩展） |
| 6 | `oryxos profile list` | 列出 Agent | `GET /profiles` |
| 7 | `oryxos profile create` | 创建 Agent 目录 + AGENT.md 模板 | `WorkspaceInitializer` |
| 8 | `oryxos profile show` | 查看 Agent 详情 | `GET /profiles` |
| 9 | `oryxos profile delete` | 删除 Agent 目录 | — |
| 10 | `oryxos provider list` | 列出已注册 Provider | `ProviderRegistry.names()` |
| 11 | `oryxos tool list` | 列出工具 | `GET /tools` |
| 12 | `oryxos session list` | 列出会话 | `sessions` 表 |

---

## 详细契约

### 1. `oryxos init`

```bash
oryxos init [--force] [--workspace .oryxos]
```

**产物结构**

```text
.oryxos/
├── AGENTS.md              # 工作区说明（全局指令，注入所有 Agent）
├── SOUL.md                # 助手人格设定
├── USER.md                # 用户画像（手写、只读）
├── agents/                # 每个子目录 = 一个 Agent
│   └── example/
│       ├── AGENT.md
│       └── skills/
├── skills/                # 公共 Skill 库（真相源）
├── memory/
│   └── MEMORY.md
├── mcp_servers.yaml
├── logs/
└── oryxos.db              # Flyway 首次启动时建表
```

| 行为 | 规则 |
| --- | --- |
| 已存在 `.oryxos/` | 默认**拒绝**并提示；`--force` 仅补缺失文件，**不覆盖**已有文件 |
| 幂等 | 重复执行不破坏现有内容（FR-051） |
| 权限 | `MEMORY.md`、`AGENT.md`、`SKILL.md` 允许用户直接编辑 |

---

### 2. `oryxos status`

```bash
oryxos status [--json]
```

**输出示例**

```text
OryxOS 1.0.0-SNAPSHOT   workspace: D:/proj/.oryxos
Providers : deepseek, qwen
Agents    : 3 (daily-report, ops-helper, hr-assistant)
Tools     : 7 (5 builtin, 2 mcp)
DB        : UP  (sessions=42, tool_invocations=318, llm_calls=127)
Server    : not running
```

> 审计计数为**只读摘要**；完整审计查询接口属扩展阶段（宪法 V 允许）。

---

### 3. `oryxos chat`

```bash
oryxos chat [--agent <name>] [--message <text>] [--session <id>] [--json]
```

| 参数 | 默认 | 说明 |
| --- | --- | --- |
| `--agent` | `default` | Agent 目录名 |
| `--message` | — | 提供则单轮退出；省略则进入交互模式 |
| `--session` | 新建 | 续接已有会话 |
| `--json` | false | 单行 JSON 输出 |

**交互模式**

```text
[agent:default] > 你好
Agent: 你好，我是……
[agent:default] > /exit
```

| 特殊命令 | 行为 |
| --- | --- |
| `/exit` `/quit` | 退出 |
| `/reset` | 新建会话 |
| `/tools` | 显示当前可见工具 |
| `/memory` | 显示长期记忆摘要 |

> `chat` 与 `serve` 共享同一份 Agent 定义与同一 SQLite，会话可跨通道续接（FR-049）。

---

### 4. `oryxos serve`

```bash
oryxos serve [--port 8080] [--host 0.0.0.0]
```

- 转发为 Spring 参数：`--server.port`、`--server.address`。
- 启动后打印监听地址与 10 个端点摘要。

### 5. `oryxos gateway`

```bash
oryxos gateway
```

本期与 `serve` 等价；多 Channel 属扩展阶段（宪法明确排除），此处仅保留入口以避免后续破坏 CLI 契约。

---

### 6-9. `oryxos profile *`

```bash
oryxos profile list                    # 名称 / provider / model / 工具数
oryxos profile create <name> [--template basic|tool-using|memory]
oryxos profile show <name> [--json]
oryxos profile delete <name> [--force]
```

| 规则 | 说明 |
| --- | --- |
| `create` | 生成 `agents/<name>/AGENT.md` + 空 `skills/`；`--template` 决定 frontmatter 预设 |
| `create` 已存在 | 拒绝，除非 `--force` |
| `delete` | 默认交互确认；`--force` 跳过。**不删除**该 Agent 的历史会话与审计（可追溯性） |
| 名称校验 | `^[a-z0-9][a-z0-9_-]{0,63}$`（与 data-model §2.1 一致） |

---

### 10. `oryxos provider list`

```bash
oryxos provider list [--json]
```

**输出**

```text
deepseek  base=https://api.deepseek.com  models=deepseek-chat, deepseek-reasoner  key=✓
qwen      base=https://dashscope...      models=qwen-max                        key=✗  (QWEN_API_KEY 未设置)
```

> 仅显示 key **是否存在**，绝不打印 key 本身。

---

### 11. `oryxos tool list`

```bash
oryxos tool list [--agent <name>] [--json]
```

| 参数 | 说明 |
| --- | --- |
| `--agent` | 只显示该 Agent 可见集合及其来源 |
| 省略 | 显示全局池 |

> `--agent` 输出需能区分 `BUILTIN` / `MCP` / `PLUGIN_ANNOTATION` / `AGENT_PRIVATE`（FR-030）。

---

### 12. `oryxos session list`

```bash
oryxos session list [--agent <name>] [--limit 20] [--all]
```

**输出**

```text
SESSION ID                          AGENT         STATUS    LAST ACTIVE            TURNS
http:u-1001:daily-report:9f3c       daily-report  ACTIVE    2026-10-27 08:16       6
cli:local:ops-helper:a12b           ops-helper    ARCHIVED  2026-10-26 21:03       14
```

| 参数 | 说明 |
| --- | --- |
| `--limit` | 默认 20 |
| `--all` | 含 `ARCHIVED`（默认只列 `ACTIVE`） |

---

## CLI 契约测试清单

| # | 用例 | 断言 |
| --- | --- | --- |
| L-01 | `init` 于空目录 | 退出 0，目录结构完整 |
| L-02 | `init` 重复执行 | 退出 0，已有文件内容不变（FR-051） |
| L-03 | `init --force` 于有 `MEMORY.md` 的目录 | 不覆盖 `MEMORY.md` |
| L-04 | `chat --message` | 退出 0，stdout 含 Agent 回复 |
| L-05 | `profile create` 重名 | 退出 1，提示存在 |
| L-06 | `tool list --agent` | 输出含 `AGENT_PRIVATE` 来源项 |
| L-07 | `provider list` | 不出现任何真实 key 字符串 |
| L-08 | 未知子命令 | 退出 2（Picocli 参数错误） |
