# OryxOS

> 企业能完全掌控的、Java 原生的、私有可审计的 Agent 统一底座。

OryxOS 是一个 **Agent OS（Agent Operating System）**。它装在企业自己的 K8s、服务器或物理机上，作为统一底座跑各种业务 Agent（运维助手、客服助手、HR 助手、销售助手、知识管理助手等），共享一套渠道接入、模型路由、记忆系统、工具调用和安全审计能力。

业务方在 OryxOS 上**配置** Agent（写 Profile YAML）、**扩展** Tool（用 SKILL.md + MCP 或 @Tool 注解），**不需要写 Agent 后端代码**。

---

## 为什么是 OryxOS

企业要在生产环境跑 Agent，尤其是金融、政务、能源等严监管行业，有几条绕不过去的硬约束：

- **数据不出企业**：核心业务的 Agent 不能跑在 SaaS 上。
- **不锁生态**：不能绑定某个公有云。
- **可审计**：谁能纳入现有 IT 治理和合规流程。
- **技术栈对齐**：企业现有系统大量是 Java/Spring，Tool 要能直接调现有 Java 服务。

这些需求是刚性的、确定的。而今天的选择只有 Node.js 的 OpenClaw 和 Python 的 Hermes，Java 体系的企业只能跨技术栈写大量胶水代码，去对接自己的 Java 服务、运维工具链和审计流程。

**OryxOS 补上 Java 生态在 Agent OS 这一层的缺位**，让 Java 体系的企业有一个原生的、跟整套 Java 基础设施咬合的 Agent 底座。装上就像装一个 Spring Boot 应用一样自然。

> OryxOS 锚在"严监管企业需要一个自己能完全掌控的 Agent 底座"这个不变的企业刚需上，而不是锚在"Agent OS"这个概念上。无论技术概念如何演变，这个需求都在。

---

## 核心能力

OryxOS 围绕**五大核心能力**构建：

### 1. 对接 LLM（Provider 抽象）

统一管理所有 LLM Provider，对 Agent 屏蔽不同厂商差异。基于 Spring AI Alibaba，复用现成的主流 LLM connector（DeepSeek、通义、文心、Kimi、智谱、混元、豆包、Anthropic、OpenAI 等）。运行时切换模型，无 vendor lock-in。

### 2. ReAct 循环（Agent 大脑）

自实现的 ReAct loop，是 OryxOS 最核心的一段代码。输入一条用户消息，输出 Agent 的最终响应，中间可调用若干次 LLM 和 Tool。LLM 思考（Reason）是否调用工具，OryxOS 执行（Act）工具并把结果回填，LLM 看到结果决定下一步，直到给出最终响应。

核心循环约数十行 Java，完全可控，不依赖任何框架的 Agent 抽象。

### 3. Memory 三层记忆（让 Agent 记得住）

Agent 跨对话保留状态的能力：

- **会话记忆**：当前对话的完整历史，持久化到 SQLite，重启可恢复。
- **长期记忆**：用户偏好、项目背景、关键事实，存在 `MEMORY.md` 文件里跨对话保留。Agent 通过 `save_memory` / `recall_memory` 两个内置 Tool 主动读写。
- **情景记忆**：任务过程状态保持（*扩展阶段*）。

用一段时间后，Agent 自然记住你的偏好、项目信息、关键决策，下一次对话不需要重新解释。这是 Agent OS 区别于 chatbot 的核心体验。

### 4. Tool 体系（让 Agent 能干事）

Tool 分两类，是 OryxOS 让业务方扩展的核心机制：

**内置 Tool**（OryxOS 自带）：文件操作（read_file、write_file、list_dir）、Shell、HTTP（http_get、http_post），加上 Memory 的 save_memory、recall_memory。

**Plugin Tool**（业务方扩展），三档接入，按门槛从低到高：

| 方式 | 门槛 | 做法 |
| --- | --- | --- |
| 方式一 | 零代码 | 写一份 SKILL.md 描述任务，复用现成 MCP server，LLM 自己理解并组合 |
| 方式二 | 轻代码 | 用任何语言写 MCP server，通过 MCP 协议接入 |
| 方式三 | 重代码 | 用 @Tool 注解写 Java Spring Bean，进程内直接调用 |

选择标准：**能用方式一就不用方式二，能用方式二就不用方式三。**

### 5. Web Service（对外接口暴露）

业务系统通过 REST API 接入 OryxOS 的所有能力。这是 OryxOS 区别于个人助手项目的关键能力——企业要把 AI 能力嵌入已有产品，靠的就是 Web Service。

覆盖会话管理、Agent 调用、Profile/Memory/Tool 信息查询、系统状态六类操作。

---

## 架构

OryxOS 是一个 Spring Boot 单体应用，对外两个入口（CLI 和 Web Service），消息都汇入同一个引擎。

```
接入层   CLI Channel            Web Service (REST API)
          \                    /
引擎层      ReActLoop + PromptBuilder + ToolExecutor   ← Agent 大脑
           /          |            \
能力层  Provider    Memory         Tool
        (LLM)      (三层记忆)     (内置 + Plugin)
           \          |            /
基础层  Profile/Bootstrap/Skill 加载、Session 存储、SQLite、配置与密钥加载
```

**一句话**：Provider、Memory、Tool 三个能力供养 ReAct 循环这个引擎，引擎通过 CLI 和 Web Service 两个入口对外提供。

**关键架构特性**：所有能力收敛到一个引擎、一套存储、一个进程内，符合"单二进制、装好就跑"的定位。外部依赖（LLM 厂商 API、外部 MCP server）都在应用边界之外，OryxOS 自身不绑定任何一家。

---

## 技术栈

| 组件 | 选型 |
| --- | --- |
| 运行时 | JDK 21 |
| 应用框架 | Spring Boot 3.x（单体，Maven 多模块） |
| LLM 调用 | Spring AI + Spring AI Alibaba |
| Agent 核心 | 自实现 ReAct loop |
| HTTP 服务 | Spring MVC + Java 21 virtual thread |
| 命令行 | Picocli + SnakeYAML |
| 持久化 | SQLite + Spring Data JPA |
| MCP 集成 | MCP Java SDK |
| 日志 | Logback + SLF4J |
| 指标（扩展） | Micrometer + Prometheus |

**部署形态**：`mvn clean package` 生成 fat JAR，`java -jar` 启动。扩展阶段引入 GraalVM Native Image 编译成原生二进制，进一步压缩启动时间和内存占用。

---

## 快速开始

> 以下为 OryxOS 1.0 的目标使用方式。

### 前置要求

- JDK 21 及以上
- 一个主流 LLM 的 API key（DeepSeek、Kimi、通义等任意一家）

### 安装

```bash
# 克隆仓库
git clone https://github.com/oryxos/oryxos.git
cd oryxos

# 构建
mvn clean package

# 得到一个可执行 JAR
java -jar oryxos-boot/target/oryxos.jar --help
```

### 初始化工作区

```bash
oryxos init
```

在当前目录下创建 `.oryxos/` 工作目录：

```
.oryxos/
├── profiles/          # Profile 配置（每个 Agent 一个 YAML）
├── memory/
│   └── MEMORY.md      # 长期记忆
├── skills/            # SKILL.md 文件
├── mcp_servers.yaml   # MCP server 配置
├── sessions/          # 会话数据
├── logs/              # 结构化日志
├── AGENTS.md          # 项目级 agent 行为说明（Bootstrap）
├── SOUL.md            # 默认 agent 人格定义（Bootstrap）
├── USER.md            # 用户偏好（Bootstrap）
└── oryxos.db          # SQLite 数据库
```

### 配置 Provider

通过环境变量注入 API key（不明文写死在配置里）：

```bash
export DEEPSEEK_API_KEY=sk-xxxxxx
```

编辑 `.oryxos/profiles/default.yaml`：

```yaml
name: default
description: 默认 Agent
identity:
  agent_name: Oryx
  prompt: 你是一个乐于助人的助手。
provider:
  name: deepseek
  model: deepseek-chat
  temperature: 0.7
tools:
  - read_file
  - write_file
  - list_dir
  - http_get
  - save_memory
  - recall_memory
channels:
  - cli
bootstrap:
  - AGENTS.md
  - SOUL.md
  - USER.md
settings:
  max_iterations: 10
  max_history_turns: 20
```

### 开始对话

```bash
oryxos chat
```

```
> 查一下北京天气，告诉我穿什么
[Agent 调用 http_get 获取天气...]
北京今天晴，气温 5~15°C，建议穿薄外套...

> /quit
```

### 启动 Web 服务

```bash
oryxos serve
```

默认监听 `8080` 端口，业务系统通过 REST API 接入：

```bash
# 创建会话
curl -X POST http://localhost:8080/api/v1/sessions

# 发消息
curl -X POST http://localhost:8080/api/v1/sessions/{id}/messages \
  -H "Content-Type: application/json" \
  -d '{"message": "帮我分析一下这批日志"}'
```

### 核心 REST 端点（核心阶段 10 个）

| 端点 | 说明 |
| --- | --- |
| `POST /api/v1/sessions` | 创建会话 |
| `POST /api/v1/sessions/{id}/messages` | 发消息 |
| `GET /api/v1/sessions/{id}` | 查询会话历史 |
| `DELETE /api/v1/sessions/{id}` | 归档会话 |
| `POST /api/v1/agents/{name}/invoke` | Agent 无状态调用 |
| `GET /api/v1/profiles` | 列 Profile |
| `GET /api/v1/memory` | 查长期记忆 |
| `GET /api/v1/tools` | 列可用 Tool |
| `GET /api/v1/health` | 健康检查 |
| `GET /api/v1/info` | 系统信息 |

---

## 命令行工具

| 命令 | 说明 |
| --- | --- |
| `oryxos init` | 初始化工作区 |
| `oryxos status` | 查看配置和运行状态 |
| `oryxos chat` | 交互式对话（`--profile` 指定 Profile，`--message` 发单条消息） |
| `oryxos serve` | 启动 HTTP API 服务 |
| `oryxos gateway` | 启动多渠道守护进程 |
| `oryxos profile list` | 列出所有 Profile |
| `oryxos profile create <name>` | 创建新 Profile |
| `oryxos profile show <name>` | 查看 Profile 详情 |
| `oryxos profile delete <name>` | 删除 Profile |
| `oryxos provider list` | 列出已配置的 Provider |
| `oryxos tool list` | 列出已注册的 Tool |
| `oryxos session list` | 列出会话历史 |

---

## 扩展 Agent：Plugin Tool 三种方式

### 方式一：零代码（主推）

写一份 `.oryxos/skills/daily-pr-digest.md` 描述任务，配置需要的 MCP server，LLM 自己理解任务、自己组合调用。

```markdown
---
name: daily-pr-digest
description: 汇总昨日 GitHub PR 评审进度并推送到 Slack
trigger: 每天早上 9 点
required_tools:
  - github-mcp
  - slack-mcp
---

请拉取昨日所有 open 的 PR，按评审人分组汇总进度，
然后推送到 #dev 频道。
```

在 `.oryxos/mcp_servers.yaml` 声明要用的 MCP server（复用社区现成的）：

```yaml
servers:
  - name: github-mcp
    transport: stdio
    command: npx -y @modelcontextprotocol/server-github
  - name: slack-mcp
    transport: stdio
    command: npx -y @modelcontextprotocol/server-slack
```

创建 Profile 引用这个 Skill 和 MCP server。**整个过程不写一行代码。**

### 方式二：轻代码

用任何语言（Python、Go、Shell、Java）写一个 MCP server，通过标准 MCP 协议暴露工具，OryxOS 作为 MCP Client 连接进来。适合接入企业自有系统（ERP、CRM、CMDB）。

### 方式三：重代码

用 Spring AI 的 @Tool 注解标注 Java 方法，OryxOS 启动时自动扫描注册。适合需要直接调用企业内部 Java 服务、复用现有 Spring Bean、跟 Spring Security 集成做权限控制的场景。

```java
@Component
public class CrmTools {
    @Tool(description = "查询客户历史交易记录")
    public String queryCustomerHistory(@ToolParam(description = "客户 ID") String customerId) {
        // 直接调用企业内部 CRM 服务
        return crmService.query(customerId);
    }
}
```

---

## 项目结构

OryxOS 是 Maven 多模块项目，由 9 个模块组成：

| 模块 | 职责 |
| --- | --- |
| `oryxos-core` | 核心引擎：ReActLoop、PromptBuilder、ToolExecutor、ContextLoader、Session、Profile、OryxTool 抽象 |
| `oryxos-provider` | LLM 能力：ProviderService、Function Calling 适配、provider name 映射 |
| `oryxos-memory` | Memory 能力：MemoryService（三层统一门面）、LongTermMemory、MemoryTools |
| `oryxos-tool` | Tool 能力：内置 Tool、MCP Client、ToolRegistry、SandboxChecker |
| `oryxos-web` | Web Service：WebServer、六个 ApiController、GlobalExceptionHandler、OpenAPI 文档 |
| `oryxos-channel-cli` | CLI Channel 实现 |
| `oryxos-storage` | SQLite 存储层（sessions、tool_invocations、llm_calls 三张表） |
| `oryxos-cli` | Picocli 命令行入口（12 个子命令） |
| `oryxos-boot` | Spring Boot 启动模块，打 fat JAR |

模块之间通过接口解耦。扩展阶段加新 Channel 或新 Tool 只加新模块，不动 core。

---

## 关键设计决策

OryxOS 做了一系列明确的取舍：

| 决策 | 选择 | 理由 |
| --- | --- | --- |
| ReAct 循环 | 自己实现 | Agent 核心完全可控 |
| Spring AI 边界 | 只用协议转换和 schema 生成，**禁用自动 tool 执行** | 否则 tool 被调两次 |
| 执行模型 | 同步阻塞 + virtual thread | 代码直观又能扛并发 |
| Tool 注册 | @Tool 注解 + OryxTool 抽象层 | ReAct 不感知 Tool 来源 |
| HTTP 层 | Spring MVC + virtual thread | 单机撑几千并发 |
| Sandbox | Path/Pattern 白名单（不用 SecurityManager） | SecurityManager 在 JDK 21 已不可用 |
| 持久化 | SQLite + MEMORY.md，审计表 day one 落库 | 可审计地基从一开始立起来 |

**关于记忆的存储选型**：核心阶段用 SQLite + MEMORY.md 文件 + 关键词检索跑通最短链路，向量检索放扩展阶段（LanceDB Java GA / pgvector / JVector 三选一）。MemoryService 接口已预留升级空间，切换底层实现不影响上层。

---

## 安全

OryxOS 面向严监管企业，安全是 day one 的设计：

- **Skill 和 Tool 来源受控**：不做无约束的公开市场，企业内的 Skill 和 Tool 经过注册、审核、签名、版本管理。
- **最小权限**：每个 Agent、每个 Tool 拿到的权限是显式授予的最小集合。
- **沙箱隔离**：核心阶段用应用层白名单校验（文件路径、Shell 命令、HTTP 域名），扩展阶段补 Docker/K8s pod 容器级隔离。
- **凭证不落地**：API key、token 通过环境变量注入，不明文写死，扩展阶段对接企业 KMS/Vault。
- **全链路审计**：谁、在何时、调了哪个 Agent、用了什么 Tool、访问了什么数据，全程结构化留痕（`tool_invocations`、`llm_calls` 表 core 阶段即落库）。

---

## 路线图

OryxOS 的交付分两段。

### 核心阶段（当前）：运行时内核

交付一个**可演示的最小完整 Agent OS 运行时内核**，五大核心能力全部跑通：配置 Agent、CLI 对话、多 Agent 并存、REST API 接入、MCP 工具生态对接。能力上对齐业界开源 Agent OS 的基础层。

实施按 5 个 User Story 依赖顺序推进：

```
US-1 对接 LLM  →  US-2 ReAct 循环  →  ┬ US-3 Memory  ┐  →  US-5 Web Service
                                      └ US-4 Tool    ┘
```

### 扩展与社区阶段：企业级治理层

补齐生产级使用必需但不在最短链路上的能力，其中包含让 OryxOS 成为真正企业级 Agent OS 的治理层：

- 多 Channel（企业微信、飞书、钉钉、Slack、邮件）
- Provider fallback / circuit breaker / Adaptive Routing
- Memory 自动抽取、语义检索、情景记忆
- Tool Policy、Tool LRU 加载、完整 Sandbox 隔离
- Web Service 剩余端点、SSE 流式、认证/鉴权、RBAC
- Web 仪表板、SSO、多租户、完整审计、可观测性
- 集群化部署与高可用、企业 IT 系统 connector

**长期方向**（社区共建）：Skills Marketplace、SDK 多语言支持、可视化 Profile 编辑器、Kubernetes Operator、移动端管理台、Voice Channel、RISC-V 边缘部署等。

---

## 与相邻项目的关系

| | 框架（LangChain、Spring AI） | 编排平台（Dify、Coze） | **OryxOS** |
| --- | --- | --- | --- |
| 产物 | 代码 | 一条 workflow | **配置出来的常驻 Agent** |
| 谁来用 | 开发者写代码 | 业务人员拖拽 | **业务方配置 Agent、写 Tool** |
| 运行环境 | 自己搭 | 平台提供 | **自带，装好就跑** |
| 所在层 | 最底层组件 | 应用层 | **运行时层** |

- **跟框架的关系是复用**：OryxOS 的 LLM 调用层直接基于 Spring AI / Spring AI Alibaba。
- **跟编排平台的关系是互补**：Dify 可以跑在 OryxOS 之上（把 OryxOS 当后端运行时调）。
- **跟 OpenClaw、Hermes 是同类不同定位**：OpenClaw 偏个人（Node.js），Hermes 偏个人到小团队（Python），OryxOS 直接定位企业场景（Java），三者通过 SKILL.md 标准互通，生态互补不竞争。

OryxOS 兼容 [agentskills.io](https://agentskills.io) 开放标准，社区的优质 Skill 经过企业审查后可以导入。

---

## 文档

| 文档 | 内容 |
| --- | --- |
| [业界调研](docs/IndustryResearch.md) | 定位、愿景与边界（Why） |
| [需求文档](docs/DemandAnalysis.md) | 五大核心能力、验收标准（What） |
| [技术方案](docs/TechnicalSolution.md) | 技术栈、模块结构、关键决策（How） |
| [AI 编程实施](docs/AiProgrammingGuide.md) | Spec-Kit 实施拆解、5 个 user story |

---

## 贡献

OryxOS 目前处于核心阶段开发中。核心阶段完成后转入长期的开源社区维护，扩展功能由社区贡献者陆续推进。

主仓库会提供清晰的 issue 标注：`good-first-issue`、`feature-request`、`long-term-goal`。贡献前请阅读项目宪章（见 [CLAUDE.md](CLAUDE.md)），所有贡献的代码必须遵守其中的非协商原则。

---

## 许可

<!-- TODO: 补充开源许可证（如 Apache-2.0） -->

---

## 理念

**核心阶段把运行时内核做扎实，扩展阶段补齐企业级治理形成差异化。**

业务方在 OryxOS 上配 Profile、写 Plugin Tool、调 Web Service 就能解决自己的业务问题。OryxOS 不绑定具体业务，业务方按自己的需求组合。

四个词概括：**统一、私有、易接入、可观测**。
