---
title: Plugin Tool
description: OryxOS 扩展 Tool 的三档方式 —— 零代码、轻代码、重代码。
outline: deep
---

# Plugin Tool

OryxOS 的扩展机制叫 Plugin Tool，分三档。**选择标准：能用方式一就不用方式二，能用方式二就不用方式三。**

| 档位 | 方式 | 代码量 | 适合 |
| --- | --- | --- | --- |
| 方式一 | SKILL.md + 复用 MCP server | 零代码 | 通用任务 |
| 方式二 | 任何语言写 MCP server | 轻代码 | 企业自有系统 |
| 方式三 | `@Tool` 注解写 Java Bean | 重代码 | 深度集成 Java 服务 |

## 方式一：零代码 Skill

写一份 `SKILL.md` 描述任务，复用社区现成的 MCP server。LLM 会自己理解并组合调用。

```markdown
<!-- skills/fetch-jira/SKILL.md -->
# 查询 Jira 工单

当用户询问工单状态时，使用 jira MCP server：
1. 用 search 工具按工单号查询
2. 总结当前状态、负责人与最近一次更新
```

适合大多数通用任务。社区已有大量 MCP server 可直接复用。

## 方式二：轻代码 MCP

用任何语言（Python / Go / Shell / Java）实现一个 MCP server，通过标准 MCP 协议接入企业自有系统（ERP / CRM / CMDB）。

```python
# my_server.py
from mcp.server import Server

server = Server("cmdb")

@server.tool()
def query_asset(hostname: str) -> dict:
    """按主机名查询 CMDB 资产信息"""
    return cmdb_client.get(hostname)
```

注册到 OryxOS：

```bash
oryxos mcp add ./my_server.py
```

适合需要访问企业内部系统、但不想把逻辑写进 Java 的场景。

## 方式三：重代码 @Tool

用 `@Tool` 注解标注 Java 方法，Spring 容器里的 Bean 会被自动发现。

```java
@Component
public class HrTools {

    private final AttendanceService attendanceService;

    public HrTools(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @Tool(description = "查询某员工本月考勤情况")
    public String queryAttendance(@ToolParam("员工工号") String empNo) {
        return attendanceService.summary(empNo);
    }
}
```

适合：

- 深度集成现有 Java 服务（复用 Spring Bean）
- 需要与 Spring Security 等基础设施协作
- 需要类型安全、单元测试、IDE 支持

## 三种方式的对比

| | 方式一 | 方式二 | 方式三 |
| --- | --- | --- | --- |
| 语言 | Markdown | 任意 | Java |
| 复用社区生态 | ✅ 最佳 | ✅ | 一般 |
| 调企业自有系统 | 一般 | ✅ 最佳 | ✅ |
| 复用现有 Java Bean | ✗ | ✗ | ✅ 最佳 |
| 维护成本 | 最低 | 中 | 最高 |

## 注册与发现

所有方式注册后，均可通过 REST API 查看：

```bash
curl http://localhost:8080/api/v1/tools
```

Agent 在 ReAct 循环中会根据工具描述自主决定调用哪个工具。
