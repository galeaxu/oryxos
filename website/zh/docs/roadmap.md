---

title: 路线图
description: OryxOS 的当前状态与规划。
outline: deep
---

# 路线图

OryxOS 正在从可用走向好用。以下按阶段列出已完成与规划中的能力。

## 已实现

| 能力 | 说明 |
| --- | --- |
| Provider 抽象 | 基于 Spring AI Alibaba，支持多家国内/国外模型 |
| ReAct 循环 | 自实现，最大迭代可配 |
| 会话记忆 | SQLite 持久化 |
| 长期记忆 | MEMORY.md，内置 Tool 读写 |
| 内置 Tool | 文件 / HTTP / shell / 记忆 |
| Plugin Tool | 三档接入（Skill / MCP / @Tool）|
| Web Service | REST API |
| CLI Channel | 交互式对话 |
| 全链路审计 | 落 SQLite |

## 进行中

| 能力 | 说明 |
| --- | --- |
| 情景记忆 | 任务过程状态 |
| 更多 Channel | 邮件、IM、工单系统 |
| Profile 热加载 | 运行时更新 Agent 配置 |

## 规划中

| 能力 | 说明 |
| --- | --- |
| RBAC | 基于角色的访问控制 |
| Spring Security 集成 | 复用企业现有鉴权体系 |
| 审计导出 | 对接企业 SIEM |
| 集群模式 | 多实例横向扩展 |
| 插件市场 | 社区 Skill / MCP 分发 |

## 反馈

路线图会随企业实际需求调整。欢迎通过 GitHub Issues 提出你的场景。
