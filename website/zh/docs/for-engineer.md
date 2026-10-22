---
title: 给工程师
description: 工程师如何扩展 OryxOS —— 配置 Agent、扩展 Tool、接入业务系统。
outline: deep
---

# 给工程师

这篇文档面向要在 OryxOS 上做扩展的工程师。

OryxOS 的设计目标是：**业务方不写 Agent 后端代码。** 你做的事情主要是三类：

1. **配置 Agent**（写 Profile）
2. **扩展 Tool**（三档：Skill / MCP / @Tool）
3. **通过 Web Service 接入业务系统**（调 REST API）

## 一、配置 Agent（Profile）

一个 Profile 描述一个 Agent 用哪套模型、哪套提示词、哪些工具。

```yaml
# profiles/ops-assistant.yaml
name: ops-assistant
provider:
  name: deepseek
  model: deepseek-chat
  temperature: 0.7
systemPrompt: |
  你是一名资深 SRE，负责分析告警、定位故障、给出处置建议。
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

Provider 的 API key 从环境变量注入，不明文落地：

```bash
export DEEPSEEK_API_KEY=sk-xxxxxx
```

## 二、扩展 Tool

### 方式一：零代码 Skill（推荐优先）

写一份 `SKILL.md` 描述任务，复用社区现成的 MCP server。

```markdown
<!-- skills/fetch-jira/SKILL.md -->
# 查询 Jira 工单

当用户询问某个工单的状态时，使用 jira MCP server 的 search 工具，
按工单号查询，并总结当前状态与负责人。
```

### 方式二：轻代码 MCP

用任意语言实现一个 MCP server，接入企业自有系统。

```python
# my_server.py —— 暴露一个内部 CMDB 查询能力
from mcp.server import Server

server = Server("cmdb")

@server.tool()
def query_asset(hostname: str) -> dict:
    """按主机名查询 CMDB 资产信息"""
    return cmdb_client.get(hostname)
```

在 OryxOS 里注册：

```bash
oryxos mcp add ./my_server.py
```

### 方式三：重代码 @Tool

用 `@Tool` 注解标注 Java 方法，直接复用现有 Spring Bean。

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

Spring 容器里的该 Bean 会被 OryxOS 自动发现并注册为一个 Tool。

## 三、Web Service 接入业务系统

业务系统通过 REST API 接入 OryxOS：

```bash
# 创建会话
curl -X POST http://localhost:8080/api/v1/sessions \
  -H 'Content-Type: application/json' \
  -d '{"profile":"ops-assistant"}'

# 发消息
curl -X POST http://localhost:8080/api/v1/sessions/{id}/messages \
  -H 'Content-Type: application/json' \
  -d '{"content":"分析一下最近的磁盘告警"}'
```

完整接口见 [REST API](/zh/docs/rest-api)。

## 选择标准的速记

> 能用方式一就不用方式二，能用方式二就不用方式三。

- 通用任务 → **Skill**
- 企业自有系统 → **MCP**
- 深度集成 Java 服务 → **@Tool**

## 安全与审计

所有工具调用与对话都落 SQLite，可查可审。生产环境的鉴权与权限控制见 [安全与审计](/zh/docs/security)。
