---
title: 核心能力
description: OryxOS 的五大核心能力。
outline: deep
---

# 核心能力

OryxOS 的五大能力供养同一个引擎，所有 Agent 共享。

## 一、对接 LLM（Provider 抽象）

基于 Spring AI Alibaba 封装 Provider 抽象，运行时按 Profile 切换模型，代码不变。

```yaml
provider:
  name: deepseek
  model: deepseek-chat
  temperature: 0.7
```

支持的模型：

| 类别 | 模型 |
| --- | --- |
| 国内 | DeepSeek / 通义 / 文心 / Kimi / 智谱 / 混元 / 豆包 |
| 国外 | Claude / GPT |

**设计要点**：无 vendor lock-in。换模型只改配置，不动业务代码。

## 二、ReAct 循环（Agent 大脑）

自实现的推理循环，完全可控：

```text
Reason → 判断是否需要调用 Tool（LLM 思考）
  ↓
Act   → 执行 Tool，回填结果
  ↓
Observe → 看到结果，决定下一步
  ↓
重复，直到给出最终响应
```

- 最大迭代次数默认 10，Profile 可配。
- 每一步都落库，可回放整个推理过程。

## 三、三层记忆

| 层 | 内容 | 存储 |
| --- | --- | --- |
| 会话记忆 | 当前对话历史 | SQLite |
| 长期记忆 | 用户偏好、项目背景 | MEMORY.md |
| 情景记忆 | 任务过程状态 | 规划中 |

Agent 通过内置 Tool 主动读写长期记忆：

```text
save_memory("用户偏好 Go，项目叫 oryxos")
recall_memory("用户偏好")
```

用一段时间后，Agent 会自然记住你。

## 四、Tool 体系

### 内置 Tool

| Tool | 说明 |
| --- | --- |
| `read_file` / `write_file` / `list_dir` | 文件操作 |
| `http_get` / `http_post` | HTTP 请求 |
| `shell` | 执行命令 |
| `save_memory` / `recall_memory` | 长期记忆读写 |

### Plugin Tool（三档）

| 档位 | 方式 | 适合 |
| --- | --- | --- |
| 方式一 | 写 SKILL.md，复用 MCP server | 通用任务 |
| 方式二 | 任何语言写 MCP server | 企业自有系统 |
| 方式三 | `@Tool` 注解写 Java Bean | 深度集成 Java |

详见 [Plugin Tool](/zh/docs/plugin-tools)。

## 五、Web Service（REST API）

把上面所有能力通过 REST API 暴露，业务系统可直接调用：

```bash
POST /api/v1/sessions
POST /api/v1/sessions/{id}/messages
POST /api/v1/agents/{name}/invoke
GET  /api/v1/tools
GET  /api/v1/memory
GET  /api/v1/health
```

完整列表见 [REST API](/zh/docs/rest-api)。

## 能力矩阵

| 能力 | 状态 |
| --- | --- |
| Provider 抽象（多模型）| ✅ 已实现 |
| ReAct 循环 | ✅ 已实现 |
| 会话记忆 | ✅ 已实现 |
| 长期记忆 | ✅ 已实现 |
| 内置 Tool | ✅ 已实现 |
| Plugin Tool（三档）| ✅ 已实现 |
| Web Service | ✅ 已实现 |
| 情景记忆 | 🔜 规划中 |
