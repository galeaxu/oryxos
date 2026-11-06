# Quickstart: 核心阶段 Agent OS 运行时内核

**Feature**: `001-core-agent-os-kernel` | **Date**: 2026-10-27

本指南用于**端到端验证**本特性是否达成 spec 中的验收目标（SC-001 ~ SC-009）。每条验证都可独立执行。

---

## 0. 前置条件

| 项 | 要求 |
| --- | --- |
| JDK | 21+ |
| Maven | 3.9+ |
| LLM API Key | 至少一个 Provider（如 `DEEPSEEK_API_KEY`） |
| 网络 | 若走 MCP / HTTP 工具，需对应服务可达 |
| OS | Linux / macOS（Windows 需开发者模式以创建软连接） |

```bash
export DEEPSEEK_API_KEY=sk-xxxx        # PowerShell: $env:DEEPSEEK_API_KEY="sk-xxxx"
```

> **安全提示**：本期无认证，请勿将 `serve` 直接暴露公网。

---

## 1. 构建

```bash
mvn clean package -DskipTests
```

产物：`oryxos-boot/target/oryxos.jar`

```bash
alias oryxos='java -jar oryxos-boot/target/oryxos.jar'
```

---

## 2. 初始化工作区 → 验证 SC-001 前置

```bash
mkdir demo && cd demo
oryxos init
oryxos status
```

**期望**：生成 §1 目录结构；`status` 显示 providers / agents / tools / DB 状态，无报错。

> 首次启动 Flyway 会建 `sessions` / `tool_invocations` / `llm_calls` 三表。
> 校验：`sqlite3 .oryxos/oryxos.db ".tables"` 应含 `flyway_schema_history`。

---

## 3. 定义一个 Agent → 验证 SC-001

```bash
oryxos profile create weather-bot --template tool-using
```

编辑 `.oryxos/agents/weather-bot/AGENT.md`：

```markdown
---
name: weather-bot
description: 天气查询与日报助手
provider:
  name: deepseek
  model: deepseek-chat
tools:
  - http_get
  - write_file
---

你是一名天气助手。查完天气后，把结果写入 report.md。
```

**验证 SC-001（Agent 定义与校验）**

```bash
oryxos profile show weather-bot      # ✅ 正常显示
# 故意改成未注册 provider 再试：
#   provider.name: gpt5
oryxos status                         # ✅ 应失败并提示 "未注册，已注册：deepseek, ..."
```

---

## 4. 单轮对话 + 工具调用 → 验证 SC-002 / SC-004

```bash
oryxos chat --agent weather-bot --message "帮我查一下北京今天天气，然后写到 report.md"
```

**期望**

1. Agent 先调用 `http_get`，再调用 `write_file`。
2. 终端输出最终答复。
3. `report.md` 已生成且内容合理。

**审计验证（宪法 V / SC-008）**

```bash
sqlite3 .oryxos/oryxos.db \
  "SELECT tool_name, status, duration_ms FROM tool_invocations ORDER BY started_at;"
# ✅ http_get | SUCCESS
# ✅ write_file | SUCCESS

sqlite3 .oryxos/oryxos.db \
  "SELECT provider, model, total_tokens, iteration FROM llm_calls ORDER BY started_at;"
# ✅ 至少 2 行，iteration 从 0 递增
```

> **关键回归检查**：每次工具调用**只能有一条** `tool_invocations` 记录。若出现两条同名同参记录，说明 Spring AI 自动执行未被禁用（宪法 II 违规）。

---

## 5. 沙箱拒绝 → 验证 SC-008

```bash
oryxos chat --agent weather-bot --message "读取 /etc/passwd 并告诉我内容"
```

**期望**

- Agent 收到失败回填（如「路径越界」），**进程不崩溃**，最终给出合理答复。
- 审计中出现 `DENIED`：

```bash
sqlite3 .oryxos/oryxos.db \
  "SELECT tool_name, status, error FROM tool_invocations WHERE status='DENIED';"
```

- 目标文件**未被读取**（输出中不含其内容）。

**同理验证 Shell 绕过**（应被结构符规则拒绝）：

```bash
oryxos chat --agent weather-bot --message "执行命令：git; rm -rf /tmp/important"
```

---

## 6. 多轮会话 + 跨重启恢复 → 验证 SC-003 / SC-006

```bash
SID=$(oryxos session list --json | jq -r '.[0].sessionId')

oryxos chat --agent weather-bot --session "$SID" --message "我偏好用中文回答"
# 重启后继续（模拟服务重启）
oryxos chat --agent weather-bot --session "$SID" --message "我刚才说了什么偏好？"
```

**期望**：第二次能正确复述偏好 → 会话历史已持久化。

**直接核对库**

```bash
sqlite3 .oryxos/oryxos.db "SELECT session_id, status, last_active_at FROM sessions;"
```

---

## 7. 长期记忆 → 验证 SC-003 / Demo 二

```bash
oryxos chat --agent weather-bot --message "记住：我负责华东区运营，报表要含同比"
cat .oryxos/memory/MEMORY.md          # ✅ 出现新条目（含日期 header）

oryxos chat --agent weather-bot --message "我负责哪个区域？"
# ✅ 能引用长期记忆回答
```

**验证截断**：连续写入至超过 4000 字，再次读取应保留**最近**内容。

---

## 8. Skill 渐进披露 → 验证 SC-004

```bash
mkdir -p .oryxos/skills/weather-guide
cat > .oryxos/skills/weather-guide/SKILL.md <<'EOF'
---
name: weather-guide
description: 气象数据解读规范，需要解释专业气象术语时使用
---
详细规范见 references/format.md
EOF

mkdir -p .oryxos/skills/weather-guide/references
echo "术语表：..." > .oryxos/skills/weather-guide/references/format.md

cd .oryxos/agents/weather-bot/skills
ln -s ../../skills/weather-guide weather-guide
cd -
```

**期望**

- `oryxos tool list --agent weather-bot` **不出现** `weather-guide`（Skill 不是 Tool）。
- 上下文只含其 `description`；正文与 `references/` 未加载。

**一致性失败用例**

```bash
ln -s /etc .oryxos/agents/weather-bot/skills/bad-skill   # 绝对 + 逃逸
oryxos status                                             # ✅ 报错：invalid-target / escaped
```

---

## 9. Web Service 同步调用 → 验证 SC-005 / Demo 四

```bash
oryxos serve --port 8080 &
```

```bash
# ① 创建会话
SID=$(curl -s -X POST localhost:8080/api/v1/sessions \
  -H 'Content-Type: application/json' \
  -d '{"agentName":"weather-bot","channel":"http","userId":"u-1001"}' | jq -r .sessionId)

# ② 发消息
curl -s -X POST localhost:8080/api/v1/sessions/$SID/messages \
  -H 'Content-Type: application/json' \
  -d '{"message":"北京今天天气如何？"}' | jq

# ③ 查历史
curl -s localhost:8080/api/v1/sessions/$SID | jq '.messages | length'

# ④ 归档
curl -s -o /dev/null -w '%{http_code}\n' -X DELETE localhost:8080/api/v1/sessions/$SID   # ✅ 204

# ⑤ 归档后再发消息
curl -s -X POST localhost:8080/api/v1/sessions/$SID/messages \
  -H 'Content-Type: application/json' -d '{"message":"hi"}' | jq .error.code   # ✅ SESSION_ARCHIVED
```

---

## 10. 多端点联动 → 验证 Demo 五 / SC-005

```bash
curl -s localhost:8080/api/v1/info    | jq          # ②
curl -s localhost:8080/api/v1/profiles | jq         # ③
curl -s localhost:8080/api/v1/tools?agentName=weather-bot | jq '.tools[].source'  # ④ 含 BUILTIN
curl -s -X POST localhost:8080/api/v1/agents/weather-bot/invoke \
  -H 'Content-Type: application/json' \
  -d '{"message":"一句话说明你能做什么"}' | jq       # ⑤
curl -s 'localhost:8080/api/v1/memory?agentName=weather-bot' | jq '.content'      # ⑥
```

**期望**：6 个端点全部 200，且 `invoke` 不创建会话（库中 `sessions` 计数不变），但 `llm_calls` 有新增记录且 `session_id IS NULL`。

---

## 11. 并发与健康 → 验证 SC-009

```bash
# 10 个 Agent 并存
for i in $(seq 1 10); do oryxos profile create agent-$i --template basic; done
oryxos status            # ✅ agents=10+

# 100 并发会话（简单压测）
seq 1 100 | xargs -P 20 -I{} curl -s -o /dev/null -X POST localhost:8080/api/v1/sessions \
  -H 'Content-Type: application/json' -d '{"agentName":"weather-bot"}'

curl -s localhost:8080/api/v1/health | jq   # ✅ status=UP
```

---

## 12. 验收对照表

| SC | 验证章节 | 关键断言 |
| --- | --- | --- |
| SC-001 | §2、§3 | Agent 定义被校验，错误明确指出问题 |
| SC-002 | §4 | 多轮工具调用完成，最终答复基于工具结果 |
| SC-003 | §6、§7 | 会话与长期记忆跨重启保留 |
| SC-004 | §8 | Skill 零代码接入；**不出现在工具列表** |
| SC-005 | §9、§10 | 10 端点链路跑通 |
| SC-006 | §6 | 重启后会话可直接续接 |
| SC-008 | §5 | 越权调用被拒且落 `DENIED` 审计 |
| SC-009 | §11 | 10 Agent / 100 并发正常 |
| 宪法 V | §4 | 每工具调用恰好一条审计 |
| 宪法 II | §4 | 无重复执行 |

---

## 13. 常见问题

| 现象 | 原因 | 处理 |
| --- | --- | --- |
| `PROVIDER_NOT_REGISTERED` | 环境变量未设置或 provider 名拼错 | `oryxos provider list` 核对 |
| 启动失败 `Schema-validation` | 手工改了 `oryxos.db` 结构 | 删除 db 重建，或新增 `V2__` 迁移 |
| Windows 创建软连接失败 | 无开发者模式 | 开启开发者模式或管理员终端 |
| 工具被调用两次 | Spring AI 自动执行未禁用（宪法 II） | 检查启动类排除项与 `ToolCallAdapter` |
| `AGENT_TIMEOUT` 504 | 模型响应慢或工具阻塞 | 检查 Provider 网络；确认工具未卡死 |
| 对话变慢、上下文变长 | `maxHistoryTurns` 过大 | 在 `AGENT.md` 调小（默认 20） |
