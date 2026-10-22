---
title: 文档
description: OryxOS 文档 —— 企业能完全掌控的、Java 原生的、私有可审计的 Agent 统一底座。
---

# OryxOS 文档

OryxOS 是一个 Agent 操作系统的**统一底座**。它装在企业自己的 K8s、服务器或物理机上，作为唯一底座跑各种业务 Agent，共享一套渠道接入、模型路由、记忆系统、工具调用和安全审计能力。业务方配置 Agent、扩展 Tool，不写 Agent 后端代码。

## 从哪里开始

| 目标 | 阅读 |
| --- | --- |
| 了解 OryxOS 是什么、为谁而生 | [OryxOS 是什么](/zh/docs/what) |
| 作为工程师理解如何扩展 | [给工程师](/zh/docs/for-engineer) |
| 跑起来 | [快速开始](/zh/docs/quick-start) |
| 理解内部结构 | [系统架构](/zh/docs/architecture) |

## 定位

| 维度 | OryxOS |
| --- | --- |
| 语言 | Java 21 / Spring Boot 3，与现有 Java 体系原生咬合 |
| 部署 | 单二进制，装在企业自有环境，数据不出门 |
| 定位 | Agent 的**底座**（OS），而非某个 Agent 应用 |
| 模型 | 通过 Provider 抽象对接多家 LLM，运行时切换，无 vendor lock-in |
| 审计 | 全链路对话与工具调用落 SQLite，可查可审 |
| 工具 | 内置 Tool + Plugin Tool 三档接入（零代码 / 轻代码 / 重代码）|

## 核心能力

1. **对接 LLM（Provider 抽象）** —— Spring AI Alibaba，运行时切换模型。
2. **ReAct 循环** —— 自实现、完全可控的 Agent 大脑。
3. **三层记忆** —— 会话记忆 / 长期记忆 / 情景记忆。
4. **Tool 体系** —— 内置 Tool + Plugin Tool 三档接入。
5. **Web Service** —— REST API 暴露所有能力，业务系统无缝接入。
