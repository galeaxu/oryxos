# REST API Contract: 核心阶段 Agent OS 运行时内核

**Feature**: `001-core-agent-os-kernel` | **Date**: 2026-10-27
**Base Path**: `/api/v1` | **Binding**: `oryxos-web` | **FR**: FR-036 ~ FR-041

## 通用约定

| 项 | 规则 |
| --- | --- |
| 内容类型 | `application/json; charset=utf-8` |
| 时间格式 | ISO-8601 UTC，如 `2026-10-27T08:15:30.123Z` |
| 认证 | **本期无认证**（宪法 VII / Assumptions）；部署须限制在内网或网关后 |
| 幂等 | 仅 `GET` / `DELETE`；`POST /sessions` 每次新建 |
| 并发模型 | Spring MVC + 虚拟线程，**同步阻塞**，无 SSE / WebSocket |
| 单条消息上限 | 32 KB（FR-038），超限 400 |
| Agent 调用超时 | 60 s（FR-038），超时 504 |
| 历史返回上限 | 100 条（FR-038） |

### 错误响应体（统一）

```json
{
  "error": {
    "code": "PROVIDER_NOT_REGISTERED",
    "message": "Agent 'daily-report' 引用的 provider 'gpt5' 未注册，已注册：deepseek, qwen",
    "traceId": "3f2a9c1e"
  }
}
```

### 错误码表

| HTTP | `code` | 触发条件 | FR |
| --- | --- | --- | --- |
| 400 | `INVALID_REQUEST` | 缺参、JSON 非法、消息 > 32KB | FR-038 |
| 400 | `PROVIDER_NOT_REGISTERED` | Profile 引用未注册 provider | FR-005/008 |
| 404 | `AGENT_NOT_FOUND` | `{name}` 无对应 Agent 目录 | FR-039 |
| 404 | `SESSION_NOT_FOUND` | `{id}` 不存在 | FR-039 |
| 404 | `TOOL_NOT_FOUND` | 查询的 tool 不存在 | FR-039 |
| 409 | `SESSION_ARCHIVED` | 向已归档会话发消息 | FR-041 |
| 409 | `VERSION_CONFLICT` | 乐观锁冲突且重试失败 | 并发 |
| 429 | `TOOL_RETRY_EXHAUSTED` | 工具重试耗尽（回填模型，非直接返回） | FR-015 |
| 500 | `INTERNAL_ERROR` | 未预期异常 | — |
| 503 | `PROVIDER_UNAVAILABLE` | LLM 调用失败 | FR-010 |
| 504 | `AGENT_TIMEOUT` | 超过 60 s | FR-038 |

---

## 1. 会话管理（4 个端点）

### 1.1 `POST /api/v1/sessions` — 创建会话

**Request**

```json
{
  "agentName": "daily-report",
  "channel": "http",
  "userId": "u-1001"
}
```

| 字段 | 必填 | 校验 |
| --- | --- | --- |
| `agentName` | ✅ | 必须存在对应 Agent 目录，否则 404 `AGENT_NOT_FOUND` |
| `channel` | ❌ | 默认 `"http"` |
| `userId` | ❌ | 任意字符串 |

**Response `201 Created`**

```json
{
  "sessionId": "http:u-1001:daily-report:9f3c",
  "agentName": "daily-report",
  "channel": "http",
  "userId": "u-1001",
  "status": "ACTIVE",
  "createdAt": "2026-10-27T08:15:30Z"
}
```

---

### 1.2 `POST /api/v1/sessions/{id}/messages` — 发送消息并取回复

> 同步返回最终答复。内部驱动完整 `ReActLoop`（可能含多次工具调用）。

**Request**

```json
{ "message": "帮我查一下上海明天天气，然后写一份日报" }
```

**Response `200 OK`**

```json
{
  "sessionId": "http:u-1001:daily-report:9f3c",
  "reply": "上海明天多云转晴，18~25℃，已写入 daily-report-2026-10-28.md。",
  "iterations": 3,
  "toolCalls": [
    { "toolName": "http_get", "status": "SUCCESS", "durationMs": 412 },
    { "toolName": "write_file", "status": "SUCCESS", "durationMs": 7 }
  ]
}
```

| 字段 | 说明 |
| --- | --- |
| `iterations` | 实际迭代轮数；达到 `maxIterations` 时 `terminated=true` |
| `toolCalls` | 本轮内工具调用摘要（完整审计在库中） |

**错误**: 409 `SESSION_ARCHIVED`、504 `AGENT_TIMEOUT`、503 `PROVIDER_UNAVAILABLE`

---

### 1.3 `GET /api/v1/sessions/{id}` — 查询历史

**Response `200 OK`**

```json
{
  "sessionId": "http:u-1001:daily-report:9f3c",
  "agentName": "daily-report",
  "status": "ACTIVE",
  "createdAt": "2026-10-27T08:15:30Z",
  "lastActiveAt": "2026-10-27T08:16:02Z",
  "truncated": false,
  "messages": [
    { "role": "user", "content": "帮我查一下上海明天天气…", "timestamp": "…" },
    { "role": "assistant", "content": "…", "timestamp": "…", "toolCalls": [ … ] },
    { "role": "tool", "content": "…", "timestamp": "…", "toolCallId": "call_abc" }
  ]
}
```

- `messages` 最多 100 条（取最近）；`truncated=true` 表示还有更早内容未返回。

---

### 1.4 `DELETE /api/v1/sessions/{id}` — 归档会话

**Response `204 No Content`**（幂等：已归档再删仍 204）

> 归档后保留审计记录；`tool_invocations` / `llm_calls` **不受影响**（宪法 V）。

---

## 2. Agent 调用（1 个端点）

### 2.1 `POST /api/v1/agents/{name}/invoke` — 无状态调用

**Request**

```json
{ "message": "总结一下今天的告警", "sessionId": "http:u-1001:daily-report:9f3c" }
```

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| `message` | ✅ | ≤ 32KB |
| `sessionId` | ❌ | 提供则续接该会话；省略则**不落会话历史**，但审计照常写入（`session_id=NULL`） |

**Response `200 OK`**

```json
{ "agentName": "daily-report", "reply": "…", "iterations": 2 }
```

**错误**: 404 `AGENT_NOT_FOUND`、400 `PROVIDER_NOT_REGISTERED`、504 `AGENT_TIMEOUT`

---

## 3. 信息查询（3 个端点）

### 3.1 `GET /api/v1/profiles` — 列出 Agent

**Response `200 OK`**

```json
{
  "profiles": [
    { "name": "daily-report", "description": "生成日报", "provider": "deepseek",
      "model": "deepseek-chat", "tools": ["http_get","write_file"], "skills": ["weather-api"] }
  ]
}
```

### 3.2 `GET /api/v1/memory` — 查询长期记忆

| Query | 必填 | 说明 |
| --- | --- | --- |
| `agentName` | ✅ | 指定 Agent |
| `q` | ❌ | 关键词；省略则返回全文（受 4000 字截断限制） |

**Response `200 OK`**

```json
{ "agentName": "daily-report", "content": "## 2026-10-26\n用户偏好 Spring Boot…", "matched": true }
```

### 3.3 `GET /api/v1/tools` — 列出工具

| Query | 必填 | 说明 |
| --- | --- | --- |
| `agentName` | ❌ | 提供则返回该 Agent 的**可见集合**（FR-030）；否则返回全局池 |

**Response `200 OK`**

```json
{
  "tools": [
    { "name": "http_get", "description": "发起 HTTP GET 请求", "source": "BUILTIN" },
    { "name": "weather.query", "description": "查询天气", "source": "MCP" }
  ]
}
```

> `source` ∈ `BUILTIN` / `MCP` / `PLUGIN_ANNOTATION` / `AGENT_PRIVATE`（FR-030 要求可列出全部及来源）。

---

## 4. 系统状态（2 个端点）

### 4.1 `GET /api/v1/health`

**Response `200 OK`**

```json
{ "status": "UP", "agents": 3, "tools": 7, "db": "UP" }
```

### 4.2 `GET /api/v1/info`

**Response `200 OK`**

```json
{
  "name": "OryxOS",
  "version": "1.0.0-SNAPSHOT",
  "javaVersion": "21",
  "workspace": "D:/proj/.oryxos",
  "endpoints": 10,
  "providers": ["deepseek", "qwen"]
}
```

---

## 5. 契约测试清单（MockMvc）

| # | 用例 | 断言 |
| --- | --- | --- |
| C-01 | 创建会话，未知 agent | 404 + `AGENT_NOT_FOUND` |
| C-02 | 发消息超 32KB | 400 + `INVALID_REQUEST` |
| C-03 | 向 ARCHIVED 会话发消息 | 409 + `SESSION_ARCHIVED` |
| C-04 | Agent 引用未注册 provider | 400 + `PROVIDER_NOT_REGISTERED`，消息含已注册列表 |
| C-05 | 工具触发沙箱拒绝 | 200（模型可继续），且库中存在 `DENIED` 审计行 |
| C-06 | `GET /tools?agentName=` | 只返回该 Agent 可见集合，含 `AGENT_PRIVATE` 项 |
| C-07 | 调用超 60 s（桩 Provider 休眠） | 504 + `AGENT_TIMEOUT` |
| C-08 | 无状态 invoke | 200，且 `llm_calls.session_id` 为 NULL、记录存在 |
