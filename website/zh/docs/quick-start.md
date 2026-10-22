---
title: 快速开始
description: 五分钟把 OryxOS 跑起来。
outline: deep
---

# 快速开始

## 前置要求

| 依赖 | 版本 |
| --- | --- |
| JDK | 21+ |
| Maven | 3.9+ |

## 构建

```bash
git clone https://gitee.com/robustmq/oryxos.git
cd oryxos
mvn clean package
```

构建产物是一个 fat JAR：`oryxos-boot/target/oryxos.jar`。

## 初始化

```bash
java -jar oryxos-boot/target/oryxos.jar init
```

`init` 会在当前目录创建 OryxOS 工作区：

```text
.oryxos/
├── profiles/      # Agent 配置
├── skills/        # Skill 定义
├── memory/        # 长期记忆（MEMORY.md）
└── oryxos.db      # 会话存储（SQLite）
```

## 配置模型

设置 Provider 的密钥，从环境变量注入：

```bash
export DEEPSEEK_API_KEY=sk-xxxxxx
```

## 开始对话

### CLI 模式

```bash
java -jar oryxos-boot/target/oryxos.jar chat
```

进入交互式对话，Agent 会在需要时自动调用工具。

### Web 服务模式

```bash
java -jar oryxos-boot/target/oryxos.jar serve
```

默认监听 `http://localhost:8080`，业务系统通过 [REST API](/zh/docs/rest-api) 接入。

```bash
curl http://localhost:8080/api/v1/health
```

## 换个模型

在 Profile 里改 Provider 即可，代码不变：

```yaml
provider:
  name: kimi
  model: moonshot-v1-8k
```

支持 DeepSeek / 通义 / 文心 / Kimi / 智谱 / 混元 / 豆包 / Claude / GPT。

## 下一步

- [系统架构](/zh/docs/architecture) —— 理解内部结构
- [Plugin Tool](/zh/docs/plugin-tools) —— 扩展 Tool 的三档方式
- [REST API](/zh/docs/rest-api) —— 接入业务系统
