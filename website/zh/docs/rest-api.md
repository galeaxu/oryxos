---
title: REST API
description: OryxOS Web Service 暴露的 REST API。
outline: deep
---

# REST API

启动 Web 服务模式后，OryxOS 通过 REST API 暴露全部能力：

```bash
java -jar oryxos-boot/target/oryxos.jar serve
# 默认监听 http://localhost:8080
```

所有响应为 JSON。错误响应形如：

```json
{ "error": "session not found" }
```

## 会话管理

### 创建会话

```http
POST /api/v1/sessions
Content-Type: application/json

{ "profile": "ops-assistant" }
```

### 发送消息

```http
POST /api/v1/sessions/{id}/messages
Content-Type: application/json

{ "content": "分析一下最近的磁盘告警" }
```

返回 Agent 的最终响应，以及本次涉及的推理与工具调用记录。

### 查询会话历史

```http
GET /api/v1/sessions/{id}
```

### 归档会话

```http
DELETE /api/v1/sessions/{id}
```

## Agent 与能力

### 无状态调用 Agent

```http
POST /api/v1/agents/{name}/invoke
Content-Type: application/json

{ "input": "帮我查一下我在 CMDB 里的机器" }
```

不创建会话，一次性调用。适合无上下文的工具型调用。

### 列出 Profiles

```http
GET /api/v1/profiles
```

### 列出已注册 Tool

```http
GET /api/v1/tools
```

### 查询长期记忆

```http
GET /api/v1/memory
```

## 系统

### 健康检查

```http
GET /api/v1/health
```

### 系统信息

```http
GET /api/v1/info
```

返回版本、已加载的 Profile 数量、已注册 Tool 数量等。

## 示例：完整调用链

```bash
# 1. 健康检查
curl http://localhost:8080/api/v1/health

# 2. 创建会话
SID=$(curl -s -X POST http://localhost:8080/api/v1/sessions \
  -H 'Content-Type: application/json' \
  -d '{"profile":"ops-assistant"}' | jq -r .id)

# 3. 发消息
curl -X POST http://localhost:8080/api/v1/sessions/$SID/messages \
  -H 'Content-Type: application/json' \
  -d '{"content":"分析一下最近的磁盘告警"}'
```
