---
title: Plugin Tools
description: OryxOS's three tiers of tool extension — no-code, light-code, heavy-code.
outline: deep
---

# Plugin Tools

OryxOS's extension mechanism is called Plugin Tools, with three tiers. **Rule of thumb: prefer Tier 1 over Tier 2, and Tier 2 over Tier 3.**

| Tier | Approach | Code volume | Best for |
| --- | --- | --- | --- |
| Tier 1 | SKILL.md + reuse MCP servers | no-code | General tasks |
| Tier 2 | Write an MCP server in any language | light-code | Enterprise systems |
| Tier 3 | Annotate a Java Bean with `@Tool` | heavy-code | Deep Java integration |

## Tier 1: No-code Skill

Write a `SKILL.md` describing the task, reuse community MCP servers. The LLM understands and composes the calls.

```markdown
<!-- skills/fetch-jira/SKILL.md -->
# Query a Jira ticket

When the user asks about a ticket's status, use the jira MCP server:
1. search by ticket id
2. summarize status, assignee and last update
```

Best for most general tasks. A large ecosystem of MCP servers is available to reuse.

## Tier 2: Light-code MCP

Implement an MCP server in any language (Python / Go / Shell / Java) to expose enterprise systems (ERP / CRM / CMDB) via the standard MCP protocol.

```python
# my_server.py
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

Best when you need to reach internal systems without writing the logic in Java.

## Tier 3: Heavy-code @Tool

Annotate a Java method with `@Tool`; any such Bean in the Spring container is auto-discovered.

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

Best for:

- deep integration with existing Java services (reuse Spring Beans)
- cooperation with infrastructure such as Spring Security
- type safety, unit testing, IDE support

## Comparing the three tiers

| | Tier 1 | Tier 2 | Tier 3 |
| --- | --- | --- | --- |
| Language | Markdown | any | Java |
| Reuse community ecosystem | best | good | fair |
| Reach enterprise systems | fair | best | best |
| Reuse existing Java Beans | no | no | best |
| Maintenance cost | lowest | medium | highest |

## Registration & discovery

After registration, all tiers are visible via REST:

```bash
curl http://localhost:8080/api/v1/tools
```

During the ReAct loop, the Agent decides which tool to call based on the tool descriptions.
