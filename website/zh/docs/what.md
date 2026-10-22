---
title: OryxOS 是什么
description: OryxOS —— 企业能完全掌控的、Java 原生的、私有可审计的 Agent 统一底座。
outline: deep
---

# OryxOS 是什么

OryxOS 是一个 **Agent 操作系统的统一底座**。

装在企业自己的 K8s、服务器或物理机上，作为唯一底座跑各种业务 Agent。所有 Agent 共享一套渠道接入、模型路由、记忆系统、工具调用和安全审计能力。业务方做的是**配置 Agent、扩展 Tool**，而不是写 Agent 后端代码。

## 为什么 OryxOS 存在

严监管行业（金融、能源、央国企）要把 Agent 真正跑进生产环境，有几条硬约束绕不过去：

- **数据主权**：核心业务的 Agent 不能跑在 SaaS 上，也不能绑定某个公有云。
- **技术栈**：企业现有系统大量是 Java / Spring，Tool 要能直接调现有 Java 服务。
- **审计**：每一次模型调用、工具调用、数据访问都要可追溯、可审计。
- **成本可控**：能私有化部署，不按 token 向外部厂商付费。

今天市面上的选择主要是 Node.js 的 OpenClaw 和 Python 的 Hermes——它们很优秀，但 Java 体系的企业要么跨技术栈写一层胶水，要么放弃。

**OryxOS 补上 Java 生态在 Agent OS 这一层的缺位。**

## 两个核心问题

**问题一：Agent 怎么接上模型和数据？**

每家企业都有自己的系统、自己的凭证、自己的模型偏好。如果没有统一底座，每个 Agent 项目都要重复解决"怎么调 LLM、密钥放哪、记忆存哪、工具怎么接入"。

**问题二：企业怎么掌控这些 Agent？**

Agent 能调用工具、能读写数据、能对外发请求。企业需要知道：谁在什么时间、用什么模型、调用了什么工具、访问了什么数据。没有统一底座，这些散落在各个项目里，无法治理。

**OryxOS 在同一个底座里解决这两个问题。**

## OryxOS 如何解决

### 一个底座，所有 Agent

OryxOS 是单体应用：一个进程里包含引擎、能力层和存储。装好就跑，不需要额外运维一堆中间件。

![OryxOS 四层架构](/diagram-architecture.svg)

### 五大核心能力

| 能力 | 详情 |
| --- | --- |
| 对接 LLM | Provider 抽象，基于 Spring AI Alibaba，运行时切换模型，无 vendor lock-in |
| ReAct 循环 | 自实现的 Agent 大脑，Reason → Act → Observe 循环，最大迭代可配 |
| 三层记忆 | 会话记忆（SQLite）/ 长期记忆（MEMORY.md）/ 情景记忆（规划中）|
| Tool 体系 | 内置 Tool + Plugin Tool 三档接入（零代码 Skill / 轻代码 MCP / 重代码 @Tool）|
| Web Service | REST API 暴露会话、Agent、Profile、工具等全部能力 |

### 扩展 Tool 的三档

选择标准很简单：**能用方式一就不用方式二，能用方式二就不用方式三。**

| 档位 | 方式 | 适合 |
| --- | --- | --- |
| 方式一（零代码）| 写一份 SKILL.md，复用社区现成的 MCP server | 大多数通用任务 |
| 方式二（轻代码）| 用任意语言写 MCP server，接入企业自有系统 | 企业内部系统对接 |
| 方式三（重代码）| 用 `@Tool` 注解写 Java Bean，复用现有 Spring Bean | 深度集成 Java 服务 |

## 定位

OryxOS **是底座，不是 Agent 应用**。它提供所有 Agent 共用的那一层能力，让业务方专注于自己的业务逻辑。

| | **OryxOS** | 直接上 LangChain / Spring AI | 商业 SaaS Agent 平台 |
| --- | --- | --- | --- |
| 私有化部署 | 单二进制，装在企业自有环境 | 需要自己搭一层 | 通常不支持 |
| Java 原生 | 是，与现有 Spring 体系咬合 | 看技术栈 | 通常非 Java |
| 统一底座 | 一个进程承载所有 Agent | 每个项目重复造轮子 | 支持但闭源 |
| 全链路审计 | day one 落库 | 需要自己实现 | 黑盒 |
| 模型可换 | Provider 抽象，运行时切换 | 需改造 | 通常绑定 |

**OryxOS 不是：**
- 通用聊天机器人框架——它是承载多个业务 Agent 的底座
- 编排框架——它负责运行 Agent，而非为 Agent 做业务决策
- 数据管道或事件总线——它面向 Agent 对话与工具调用

## 核心概念

### Profile

一个 Agent 的配置单元。描述用哪个 Provider、哪套 System Prompt、允许哪些 Tool、记忆如何配置。业务方通过 Profile 定义自己的 Agent。

### Provider

模型接入的抽象。一个 Provider 代表一个可用的 LLM（DeepSeek、通义、Kimi、智谱、Claude、GPT…）。运行时可在 Profile 中切换，代码不变。

### ReAct 循环

Agent 的推理大脑。输入一条用户消息后：**Reason（LLM 判断是否要调 Tool）→ Act（执行 Tool）→ Observe（看到结果）→ 循环**，直到给出最终响应。

### 三层记忆

- **会话记忆**：当前对话历史，SQLite 持久化。
- **长期记忆**：用户偏好、项目背景等跨会话信息，存 MEMORY.md，Agent 通过内置 Tool 主动读写。
- **情景记忆**：任务过程状态，规划中。

### Plugin Tool

OryxOS 的扩展机制。三档接入：零代码 Skill、轻代码 MCP server、重代码 `@Tool` Java Bean。

## 设计原则

**底座而非框架。** OryxOS 把"所有 Agent 都要用"的那层能力沉淀下来，业务方只写自己的业务。

**Java 原生。** 与现有 Spring / Java 生态直接咬合，不引入第二种运行时。

**单二进制。** 一个进程、一套存储，装好就跑，符合企业现有的部署与运维习惯。

**模型可换。** 通过 Provider 抽象对接多家模型，运行时切换，不被单一厂商锁定。

**审计优先。** 每一次对话与工具调用都落库，满足监管行业的可追溯要求。

## 下一步

- [给工程师](/zh/docs/for-engineer) —— 理解如何扩展 OryxOS
- [快速开始](/zh/docs/quick-start) —— 五分钟跑起来
- [系统架构](/zh/docs/architecture) —— 看清内部结构
