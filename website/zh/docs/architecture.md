---
title: 系统架构
description: OryxOS 的四层架构与模块划分。
outline: deep
---

# 系统架构

OryxOS 是**单体应用**：一个进程、一套存储、一个引擎。装好就跑，不引入额外中间件。

![OryxOS 四层架构](/diagram-architecture.svg)

## 四层结构

### 接入层（Access）

对外的入口。目前两种：

- **CLI Channel** —— 命令行交互，适合开发与运维人员。
- **Web Service** —— REST API，适合业务系统集成。

### 引擎层（Engine）

Agent 的大脑：

- **ReActLoop** —— Reason → Act → Observe 循环。
- **PromptBuilder** —— 组装 System Prompt、历史记忆与工具描述。
- **ToolExecutor** —— 执行工具调用并回填结果。

### 能力层（Capabilities）

三大能力，所有 Agent 共享：

- **Provider** —— 对接 LLM，基于 Spring AI Alibaba 的抽象。
- **Memory** —— 三层记忆（会话 / 长期 / 情景）。
- **Tool** —— 内置 Tool + Plugin Tool。

### 基础层（Foundation）

支撑以上所有能力的基础设施：

- **Profile / Bootstrap / Skill 加载** —— 启动时装配 Agent。
- **Session 存储** —— 对话历史，SQLite。
- **SQLite** —— 单文件存储，零运维。
- **配置与密钥** —— 集中管理，密钥从环境变量注入。

## 请求生命周期

一条用户消息进入 OryxOS 后的路径：

```text
用户消息
  ↓
接入层（CLI / REST）
  ↓
引擎层：PromptBuilder 组装上下文
  ↓
ReActLoop：
  Reason → LLM 思考
    ├─ 否 → 直接给出最终响应
    └─ 是 → ToolExecutor 执行 Tool → 结果回填 → 再 Reason
  ↓
接入层返回响应
  ↓
全程落库（SQLite）—— 可审计
```

## 模块划分（Maven 多模块）

| 模块 | 职责 |
| --- | --- |
| `oryxos-boot` | 启动入口、打包 fat JAR、CLI |
| `oryxos-core` | 引擎、ReAct 循环、Tool 执行 |
| `oryxos-provider` | LLM Provider 抽象与实现 |
| `oryxos-memory` | 三层记忆 |
| `oryxos-tools` | 内置 Tool 与 Plugin Tool 加载 |
| `oryxos-web` | Web Service（REST API）|

## 技术选型

| 组件 | 选型 | 理由 |
| --- | --- | --- |
| 语言 | Java 21 | 与现有企业体系一致，虚拟线程等新特性 |
| 框架 | Spring Boot 3 | 企业 Java 的事实标准 |
| 模型接入 | Spring AI Alibaba | 统一多家国内/国外模型 |
| 存储 | SQLite | 单文件、零运维、可随二进制交付 |
| 扩展协议 | MCP | 社区生态丰富，跨语言 |
| 部署 | 单二进制 | 符合企业私有化交付习惯 |
