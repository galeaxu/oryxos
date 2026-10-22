---
title: For Engineers
description: How engineers extend OryxOS — configure Agents, extend tools, integrate business systems.
outline: deep
---

# For Engineers

This page is for engineers extending OryxOS.

OryxOS is designed so that **business teams do not write Agent backend code.** You do three things:

1. **Configure Agents** (write a Profile)
2. **Extend tools** (three tiers: Skill / MCP / @Tool)
3. **Integrate business systems via Web Service** (call the REST API)

## 1. Configure an Agent (Profile)

A Profile describes which model, which prompt and which tools one Agent uses.

```yaml
# profiles/ops-assistant.yaml
name: ops-assistant
provider:
  name: deepseek
  model: deepseek-chat
  temperature: 0.7
systemPrompt: |
  You are a senior SRE. Analyze alerts, locate faults, draft remediation.
tools:
  - read_file
  - http_get
  - shell
memory:
  session: true
  longTerm: true
react:
  maxIterations: 10
```

Provider API keys are injected from environment variables, never written in plaintext:

```bash
export DEEPSEEK_API_KEY=sk-xxxxxx
```

## 2. Extend tools

### Tier 1: No-code Skill (prefer this)

Write a `SKILL.md` describing the task, reuse community MCP servers.

```markdown
<!-- skills/fetch-jira/SKILL.md -->
# Query a Jira ticket

When the user asks about a ticket's status, use the jira MCP server:
1. search by ticket id
2. summarize status, assignee and last update
```

### Tier 2: Light-code MCP

Implement an MCP server in any language to expose internal systems.

```python
# my_server.py — expose an internal CMDB query
from mcp.server import Server

server = Server("cmdb")

@server.tool()
def query_asset(hostname: str) -> dict:
    """Query CMDB asset info by hostname"""
    return cmdb_client.get(hostname)
```

Register it with OryxOS:

```bash
oryxos mcp add ./my_server.py
```

### Tier 3: Heavy-code @Tool

Annotate a Java method with `@Tool` to reuse existing Spring Beans.

```java
@Component
public class HrTools {

    private final AttendanceService attendanceService;

    public HrTools(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @Tool(description = "Query an employee's attendance this month")
    public String queryAttendance(@ToolParam("employee id") String empNo) {
        return attendanceService.summary(empNo);
    }
}
```

Any such Bean in the Spring container is auto-discovered and registered as a Tool.

## 3. Integrate business systems via Web Service

Business systems integrate via the REST API:

```bash
# Create a session
curl -X POST http://localhost:8080/api/v1/sessions \
  -H 'Content-Type: application/json' \
  -d '{"profile":"ops-assistant"}'

# Send a message
curl -X POST http://localhost:8080/api/v1/sessions/{id}/messages \
  -H 'Content-Type: application/json' \
  -d '{"content":"Analyze the recent disk alerts"}'
```

Full interface in [REST API](/docs/rest-api).

## Rule of thumb

> Prefer Tier 1 over Tier 2, and Tier 2 over Tier 3.

- General tasks → **Skill**
- Enterprise systems → **MCP**
- Deep Java integration → **@Tool**

## Security & audit

All tool calls and conversations are persisted to SQLite, queryable and auditable. For production auth and permissions, see [Security & Audit](/docs/security).
