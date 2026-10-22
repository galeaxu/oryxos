---
title: REST API
description: The REST API exposed by the OryxOS Web Service.
outline: deep
---

# REST API

After starting web service mode, OryxOS exposes all capabilities via REST:

```bash
java -jar oryxos-boot/target/oryxos.jar serve
# listens on http://localhost:8080 by default
```

All responses are JSON. Error responses look like:

```json
{ "error": "session not found" }
```

## Sessions

### Create a session

```http
POST /api/v1/sessions
Content-Type: application/json

{ "profile": "ops-assistant" }
```

### Send a message

```http
POST /api/v1/sessions/{id}/messages
Content-Type: application/json

{ "content": "Analyze the recent disk alerts" }
```

Returns the Agent's final response plus the reasoning and tool-call records involved.

### Get session history

```http
GET /api/v1/sessions/{id}
```

### Archive a session

```http
DELETE /api/v1/sessions/{id}
```

## Agents & capabilities

### Stateless Agent invocation

```http
POST /api/v1/agents/{name}/invoke
Content-Type: application/json

{ "input": "Look up my machines in CMDB" }
```

No session is created; a one-shot call. Best for context-free, tool-like invocations.

### List profiles

```http
GET /api/v1/profiles
```

### List registered tools

```http
GET /api/v1/tools
```

### Get long-term memory

```http
GET /api/v1/memory
```

## System

### Health check

```http
GET /api/v1/health
```

### System info

```http
GET /api/v1/info
```

Returns version, number of loaded Profiles, number of registered tools, and so on.

## Example: full call chain

```bash
# 1. health check
curl http://localhost:8080/api/v1/health

# 2. create a session
SID=$(curl -s -X POST http://localhost:8080/api/v1/sessions \
  -H 'Content-Type: application/json' \
  -d '{"profile":"ops-assistant"}' | jq -r .id)

# 3. send a message
curl -X POST http://localhost:8080/api/v1/sessions/$SID/messages \
  -H 'Content-Type: application/json' \
  -d '{"content":"Analyze the recent disk alerts"}'
```
