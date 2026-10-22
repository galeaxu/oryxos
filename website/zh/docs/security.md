---
title: 安全与审计
description: OryxOS 的私有化部署、密钥管理与全链路审计。
outline: deep
---

# 安全与审计

OryxOS 面向严监管行业设计，安全与审计是底座能力而非附加项。

## 私有化部署

OryxOS 是单二进制，装在企业自有环境：

- K8s、虚拟机或物理机均可。
- 数据（会话、记忆、密钥）全部留在企业内网。
- 不向外部服务回传任何数据。

## 密钥管理

Provider 的 API key **不明文落地**，从环境变量注入：

```bash
export DEEPSEEK_API_KEY=sk-xxxxxx
```

- 密钥不写入 Profile、不写入数据库、不写入日志。
- 支持通过企业的密钥管理服务（如 Vault）注入环境变量。

## 全链路审计

每一次交互都落 SQLite，可查可审：

| 审计项 | 内容 |
| --- | --- |
| 会话 | 谁、什么时间发起会话 |
| 消息 | 用户输入与 Agent 响应全文 |
| 模型调用 | 用了哪个 Provider / 模型、耗时、token 用量 |
| 工具调用 | 调用了哪个 Tool、入参、返回结果 |
| 记忆读写 | 长期记忆的写入与读取 |

审计记录可通过 REST API 查询，也可直接读取 SQLite 文件：

```bash
curl http://localhost:8080/api/v1/sessions/{id}
```

## 工具执行边界

- **内置 Tool** 的文件、命令、HTTP 操作限定在工作区内。
- **Plugin Tool** 以独立进程（MCP）运行，权限与企业现有系统对齐。
- 高危操作（如 `shell`）可在 Profile 中按 Agent 粒度关闭。

```yaml
tools:
  - read_file
  - http_get
  # shell 已关闭
```

## 数据主权

| 关注点 | OryxOS 的做法 |
| --- | --- |
| 数据位置 | 企业自有环境，不出门 |
| 模型选择 | 可选用私有化部署的模型 |
| 审计留存 | 全部落企业自有 SQLite |
| 无外部依赖 | 单二进制，不依赖外部 SaaS |

## 未来规划

- 基于角色的访问控制（RBAC）
- 与 Spring Security 的深度集成
- 审计日志导出到企业 SIEM

详见 [路线图](/zh/docs/roadmap)。
