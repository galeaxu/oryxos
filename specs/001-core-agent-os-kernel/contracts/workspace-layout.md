# Workspace Layout Contract: `.oryxos/` 工作区

**Feature**: `001-core-agent-os-kernel` | **Date**: 2026-10-27
**Owner**: `oryxos-core`（`WorkspaceInitializer`、`AgentLoader`、`ContextLoader`、`SkillBinder`）| **FR**: FR-001 ~ FR-005、FR-031 ~ FR-035、FR-051

本文件定义工作区目录结构、文件格式与一致性规则。它是 `oryxos init` 的产物契约，也是 `AgentLoader` / `ContextLoader` 的输入契约。

---

## 1. 目录结构（唯一权威）

```text
.oryxos/
├── AGENTS.md              # 工作区级指令（注入所有 Agent 的 system prompt）
├── SOUL.md                # 人格/语气设定（注入所有 Agent）
├── USER.md                # 用户画像（手写、只读；Agent 不修改）
│
├── agents/                # ★ 每个子目录 = 一个 Agent（宪法 IV 硬约束）
│   └── <agent-name>/
│       ├── AGENT.md       # ★ 唯一真相源：frontmatter=Profile，正文=该 Agent 的 system prompt
│       └── skills/        # ★ 仅存相对软连接，不存副本
│           └── <skill-name>  -> ../../skills/<skill-name>
│
├── skills/                # ★ 公共 Skill 真相源（与 Agent 目录解耦）
│   └── <skill-name>/
│       ├── SKILL.md       # frontmatter(name, description) + 正文
│       ├── references/    # 可选：按需 read_file 读取，**不预载**
│       └── scripts/       # 可选：按需 shell 执行，**不预载**
│
├── memory/
│   └── MEMORY.md          # 长期记忆（Agent 经 save_memory 写入；超 4000 字保留最近）
│
├── mcp_servers.yaml       # MCP server 声明：name / transport / command / env
├── logs/                  # 结构化日志（Logback）
└── oryxos.db              # SQLite（Flyway 管理表结构）
```

---

## 2. 文件格式契约

### 2.1 `AGENT.md`

```markdown
---
name: daily-report
description: 每日运营日报生成助手
provider:
  name: deepseek
  model: deepseek-chat
  temperature: 0.3
tools:
  - http_get
  - write_file
mcpServers:
  - weather
settings:
  maxIterations: 10
  maxHistoryTurns: 20
---

你是一名运营日报助手。优先使用 http_get 获取数据，再用 write_file 落盘。
```

| 规则 | 说明 |
| --- | --- |
| `name` | 必须等于所在目录名（否则启动报错，FR-001） |
| `description` | 必填，非空 |
| `provider.name` | 必须已注册（FR-008） |
| `provider.model` | 必须在该 provider 支持的模型集合内 |
| `tools` | 未知工具名**报错**，不静默忽略 |
| `mcpServers` | 必须存在于 `mcp_servers.yaml` |
| 正文 | **每轮现读不缓存**，改文件立即生效（宪法 IV） |
| 权限 | 用户可直接编辑（git 友好） |

### 2.2 `SKILL.md`

```markdown
---
name: weather-api
description: 查询指定城市未来 24 小时天气，适合需要实时天气数据的任务
---

## 用法
1. 调用 weather.query 获取数据
2. 需要历史对比时读取 references/history-format.md
```

| 规则 | 说明 |
| --- | --- |
| `name` | 必须等于所在目录名 |
| `description` | **唯一注入上下文的内容**（渐进披露） |
| 正文 / `references/` / `scripts/` | **不预载**；仅在 LLM 显式 `read_file` / `shell` 时进入上下文（FR-032） |

### 2.3 软连接绑定（宪法 IV 硬约束）

**正确**

```bash
cd .oryxos/agents/daily-report/skills
ln -s ../../skills/weather-api weather-api
```

**禁止**

| 反例 | 原因 | 状态 |
| --- | --- | --- |
| 复制 `SKILL.md` 到 Agent 目录 | 产生两份真相源，必然漂移 | `invalid-target` |
| `ln -s /abs/path/to/skill skill-name` | 绝对路径，跨机器不可移植 | `invalid-target` |
| `ln -s ../../../../etc/passwd` | 逃逸出 `skills/` | `escaped` |
| `chain -> a -> b` | 多级链，解析不可控 | `escaped` |
| 删除 `.oryxos/skills/foo` 但保留链接 | 悬空引用 | `dangling` |
| `SKILL.md` 中 `name` ≠ 链接名 | 元数据不一致 | `name-mismatch` |

**Windows 说明**：需开发者模式或管理员权限创建 symlink；若不可用，`init` 打印明确指引而非静默降级为复制。

---

## 3. Prompt 组装顺序（`ContextLoader` + `PromptBuilder`）

```text
[system prompt]
  1. AGENTS.md 正文            ← 工作区级指令
  2. SOUL.md 正文              ← 人格设定
  3. USER.md 正文              ← 用户画像
  4. agents/<name>/AGENT.md 正文  ← Agent 专属指令
  5. 可见 Skill 的 name + description 列表（不含正文）
  6. 长期记忆 MEMORY.md（最多 4000 字，截断保留最近）

[messages]
  7. 会话历史：最近 maxHistoryTurns 轮（默认 20），超出丢弃更早的
  8. 当前 user 消息

[tools]
  9. 可见集合的 name / description / inputSchema（由 RunWithTools 语义交给模型，执行权仍在 ReActLoop）
```

**约束**

- 1~6 每次都重新读文件，**不得缓存**。
- 第 7 步裁剪只丢弃**中间历史**，保留 system prompt 与最新消息。
- 第 9 步只做 schema 传递（D-01），不注册执行器。

---

## 4. 一致性校验（启动与 `status` 时执行）

| 检查 | 失败状态 | 处理 | FR |
| --- | --- | --- | --- |
| Agent 目录名 vs `AGENT.md.name` | `name-mismatch` | 启动失败，指出 Agent | FR-001 |
| provider 已注册 | `provider-not-registered` | 启动失败，列出已注册 | FR-008 |
| 工具名存在 | `tool-not-found` | 启动失败 | FR-004 |
| MCP server 已声明 | `mcp-not-declared` | 启动失败 | FR-004 |
| Skill 链接目标存在 | `dangling` | 启动失败，指出 Agent + Skill | FR-034 |
| Skill 链接在 `.oryxos/skills/` 内 | `escaped` | 拒绝（安全） | FR-034/046 |
| 链接是相对软连接 | `invalid-target` | 拒绝 | FR-034 |
| 私有工具定义合法 | `invalid-private-tool` | 报错并指出 Agent | FR-030c |

**失败原则**：全部 **fail-fast 且明确指出 Agent**，**不得静默丢弃**（FR-030c / FR-034）。`status` 命令可用 `--relaxed` 展示问题列表而不阻断。

---

## 5. 与 SQLite 的边界

| 数据 | 位置 | 理由 |
| --- | --- | --- |
| Agent 定义、Skill、Bootstrap、MEMORY.md、MCP 配置 | 文件系统 | 用户可直接编辑、git 跟踪、备份 |
| 会话、工具调用审计、LLM 调用审计 | SQLite | 结构化查询、事务、并发控制 |
| 日志 | `logs/` 文件 | 运维排查 |

> **不得**把 Agent 定义或 Skill 内容写入 SQLite（宪法 IV：目录配置即 Agent）。

---

## 6. 契约测试清单

| # | 用例 | 断言 |
| --- | --- | --- |
| W-01 | `init` 产物 | 结构与 §1 完全一致 |
| W-02 | AGENT.md `name` 与目录不符 | 启动失败，错误含 Agent 名 |
| W-03 | Skill 绝对软连接 | 拒绝，状态 `invalid-target` |
| W-04 | Skill 链接指向 `../..` 外 | 拒绝，状态 `escaped` |
| W-05 | 删除被引用的公共 Skill | 拒绝，返回引用 Agent 列表 |
| W-06 | 修改 AGENT.md 正文后立即对话 | 新内容生效（无缓存） |
| W-07 | MCP server 不可达 | 该工具标 `UNAVAILABLE`，其他 Agent 正常 |
