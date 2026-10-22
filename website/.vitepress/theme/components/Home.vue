<script setup>
import { computed } from 'vue'
import { useData } from 'vitepress'

const { lang } = useData()
const isZh = computed(() => lang.value === 'zh-CN')
const t = (zh, en) => isZh.value ? zh : en

const capabilities = computed(() => [
  {
    icon: '🧠',
    title: t('对接 LLM（Provider 抽象）', 'LLM Providers (Abstraction)'),
    subtitle: t('Spring AI Alibaba · 运行时切换模型 · 无 vendor lock-in', 'Spring AI Alibaba · runtime model switch · no vendor lock-in'),
    code: `# 配置一个 Provider（Profile YAML）
provider:
  name: deepseek
  model: deepseek-chat
  temperature: 0.7

# API key 从环境变量注入，不明文落地
export DEEPSEEK_API_KEY=sk-xxxxxx

# 支持：DeepSeek / 通义 / 文心 / Kimi
#       智谱 / 混元 / 豆包 / Claude / GPT`,
  },
  {
    icon: '🔁',
    title: t('ReAct 循环（Agent 大脑）', 'ReAct Loop (Agent Brain)'),
    subtitle: t('自实现 · 完全可控 · 数十行 Java', 'Self-implemented · fully controllable · dozens of lines'),
    code: `# 输入一条用户消息，输出最终响应
Reason → 判断是否需要调用 Tool
  ↓        （LLM 思考）
Act   → 执行 Tool，回填结果
  ↓
Reason → 看到结果，决定下一步
  ↓
Final → 给出最终响应

# 最大迭代次数默认 10，Profile 可配`,
  },
  {
    icon: '💾',
    title: t('三层记忆（记得住）', 'Three-Layer Memory'),
    subtitle: t('会话记忆 · 长期记忆 · 情景记忆', 'Session · long-term · episodic memory'),
    code: `# 会话记忆：当前对话历史，SQLite 持久化
# 长期记忆：用户偏好 / 项目背景 → MEMORY.md
# 情景记忆：任务过程状态（扩展阶段）

# Agent 通过内置 Tool 主动读写长期记忆
save_memory("用户偏好 Go，项目叫 oryxos")
recall_memory("用户偏好")

# 用一段时间后，Agent 自然记住你`,
  },
  {
    icon: '🧰',
    title: t('Tool 体系（能干事）', 'Tool System (Get things done)'),
    subtitle: t('内置 Tool + Plugin Tool 三档接入', 'Built-in + 3-tier Plugin Tools'),
    code: `# 内置 Tool
read_file / write_file / list_dir
http_get / http_post / shell
save_memory / recall_memory

# Plugin Tool 三种接入方式
方式一：写 SKILL.md，复用 MCP server（零代码）
方式二：任何语言写 MCP server（轻代码）
方式三：@Tool 注解写 Java Bean（重代码）`,
  },
  {
    icon: '🌐',
    title: t('Web Service（对外接口）', 'Web Service (REST API)'),
    subtitle: t('REST API · 会话 / Agent / Profile / 工具', 'REST API · sessions / agents / profiles / tools'),
    code: `# 业务系统通过 REST API 接入
POST /api/v1/sessions          # 创建会话
POST /api/v1/sessions/{id}/messages  # 发消息
POST /api/v1/agents/{name}/invoke     # Agent 调用
GET  /api/v1/tools             # 列出可用 Tool
GET  /api/v1/health            # 健康检查

# 把 AI 能力嵌入企业已有产品`,
  },
])

const scenarios = computed(() => [
  {
    num: '01',
    title: t('运维助手', 'Operations assistant'),
    desc: t('接入 CMDB、监控和工单系统，Agent 自动分析告警、定位故障、生成处置建议，全程留痕可审计。', 'Connects to CMDB, monitoring and ticketing. Agent analyzes alerts, locates faults, drafts remediation — fully audited.'),
  },
  {
    num: '02',
    title: t('客服助手', 'Customer-service assistant'),
    desc: t('基于企业知识库和工单历史，Agent 用长期记忆记住每个客户，跨会话保留上下文，回答更贴合业务。', 'Uses enterprise knowledge and ticket history. Agent remembers each customer across sessions via long-term memory.'),
  },
  {
    num: '03',
    title: t('HR 助手', 'HR assistant'),
    desc: t('访问内部 Java 服务查询考勤、假期、流程状态。用 @Tool 注解直接调用现有 Spring Bean，无需重写业务逻辑。', 'Queries attendance, leave and workflows via internal Java services. @Tool-annotated methods call existing Spring Beans directly.'),
  },
  {
    num: '04',
    title: t('销售助手', 'Sales assistant'),
    desc: t('从 CRM 拉取商机、客户画像和跟进记录，Agent 生成跟进方案，结果通过 Web Service 回写业务系统。', 'Pulls opportunities, profiles and follow-ups from CRM. Agent drafts plans; results written back to systems via Web Service.'),
  },
  {
    num: '05',
    title: t('知识管理助手', 'Knowledge-management assistant'),
    desc: t('把企业文档接入 MCP server，Agent 按语义检索并组合回答，知识留在企业内网，不出企业。', 'Connects enterprise documents via MCP server. Agent retrieves and composes answers — data never leaves the enterprise network.'),
  },
  {
    num: '06',
    title: t('多 Agent 协作', 'Multi-agent collaboration'),
    desc: t('同一底座跑多个 Agent（运维 / 客服 / HR…），共享 Provider、记忆、Tool 和安全审计，各自独立配置。', 'Run multiple Agents (ops / support / HR…) on one base — sharing providers, memory, tools and audit, each independently configured.'),
  },
])

const integrations = computed(() => [
  {
    icon: '📝',
    title: t('方式一：零代码 Skill', 'Tier 1: No-code Skill'),
    desc: t('写一份 SKILL.md 描述任务，复用社区现成的 MCP server，LLM 自己理解并组合调用。适合大多数场景。', 'Write a SKILL.md describing the task; reuse community MCP servers. The LLM understands and composes the calls. Fits most cases.'),
    langs: ['SKILL.md', 'MCP', 'agentskills.io'],
  },
  {
    icon: '📦',
    title: t('方式二：轻代码 MCP', 'Tier 2: Light-code MCP'),
    desc: t('用任何语言（Python / Go / Shell / Java）写 MCP server，通过标准 MCP 协议接入企业自有系统（ERP / CRM / CMDB）。', 'Write an MCP server in any language (Python / Go / Shell / Java) to expose enterprise systems (ERP / CRM / CMDB).'),
    langs: ['Python', 'Go', 'Java', 'Shell'],
  },
  {
    icon: '🔧',
    title: t('方式三：重代码 @Tool', 'Tier 3: @Tool Java Bean'),
    desc: t('用 @Tool 注解标注 Java 方法，直接调用企业内部 Java 服务、复用 Spring Bean、跟 Spring Security 集成。', '@Tool-annotated Java methods call internal services directly, reuse Spring Beans, integrate with Spring Security.'),
    langs: ['Java 21', 'Spring Boot', '@Tool', 'MCP SDK'],
  },
])

const protocol = computed(() => [
  {
    group: t('会话管理', 'Sessions'),
    rows: [
      { sub: 'POST /api/v1/sessions', desc: t('创建会话', 'Create a session') },
      { sub: 'POST /api/v1/sessions/{id}/messages', desc: t('发送消息，返回 Agent 响应', 'Send a message, return Agent reply') },
      { sub: 'GET /api/v1/sessions/{id}', desc: t('查询会话历史', 'Get session history') },
      { sub: 'DELETE /api/v1/sessions/{id}', desc: t('归档会话', 'Archive a session') },
    ],
  },
  {
    group: t('Agent 与能力', 'Agents & Capabilities'),
    rows: [
      { sub: 'POST /api/v1/agents/{name}/invoke', desc: t('无状态调用一个 Agent', 'Stateless Agent invocation') },
      { sub: 'GET /api/v1/profiles', desc: t('列出所有 Profile', 'List profiles') },
      { sub: 'GET /api/v1/tools', desc: t('列出已注册 Tool', 'List registered tools') },
      { sub: 'GET /api/v1/memory', desc: t('查询长期记忆', 'Get long-term memory') },
    ],
  },
  {
    group: t('系统', 'System'),
    rows: [
      { sub: 'GET /api/v1/health', desc: t('健康检查', 'Health check') },
      { sub: 'GET /api/v1/info', desc: t('系统信息', 'System info') },
    ],
  },
])
</script>

<template>
  <div class="oryx-page">

    <!-- ── HERO ── -->
    <section class="oryx-hero">
      <div class="oryx-hero-inner">
        <div class="oryx-badge">
          <span class="oryx-badge-dot"></span>
          {{ t('企业自有的 Agent 统一底座', 'A self-hosted Agent base for the enterprise') }}
        </div>

        <h1 class="oryx-title">
          <span class="oryx-title-name">OryxOS</span>
        </h1>

        <p class="oryx-title-sub">{{ t('Java 原生的 Agent OS', 'The Java-native Agent OS') }}</p>

        <p class="oryx-hero-desc">
          {{ t('OryxOS 是一个 Agent 操作系统的统一底座。装在企业自己的 K8s、服务器或物理机上，作为唯一底座跑各种业务 Agent，共享一套渠道接入、模型路由、记忆系统、工具调用和安全审计能力。业务方配置 Agent、扩展 Tool，不写 Agent 后端代码。', 'OryxOS is a unified base for an Agent operating system. Install it on your own K8s, servers or bare metal, run every business Agent on one base — sharing channels, model routing, memory, tool invocation and audit. Configure Agents and extend tools without writing Agent backend code.') }}
        </p>

        <div class="oryx-hero-actions">
          <a class="oryx-btn-primary" :href="t('/zh/docs/', '/docs/')">
            {{ t('开始使用', 'Get Started') }} →
          </a>
          <a class="oryx-btn-ghost" :href="t('/zh/docs/architecture', '/docs/architecture')">
            {{ t('系统架构', 'Architecture') }}
          </a>
          <a class="oryx-btn-ghost" href="https://github.com/robustmq/oryxos" target="_blank" rel="noopener">
            GitHub
          </a>
        </div>

        <div class="oryx-hero-note">
          {{ t('JDK 21 · Spring Boot 3 · Spring AI Alibaba · SQLite · MCP · 单二进制部署', 'JDK 21 · Spring Boot 3 · Spring AI Alibaba · SQLite · MCP · single-binary deploy') }}
        </div>
      </div>
    </section>

    <!-- ── PROBLEM ── -->
    <section class="oryx-section">
      <div class="oryx-section-inner">
        <div class="oryx-problem">
          <div class="oryx-problem-text">
            <h2 class="oryx-section-title">{{ t('为什么需要 OryxOS', 'Why OryxOS') }}</h2>
            <p>{{ t('严监管行业要在生产环境跑 Agent，有几条绕不过去的硬约束。', 'Regulated industries face non-negotiable constraints when running Agents in production.') }}</p>
            <p class="oryx-problem-item">
              <strong>{{ t('① 数据不出企业', '① Data must not leave the enterprise') }}</strong>
              {{ t('核心业务的 Agent 不能跑在 SaaS 上，也不能绑定某个公有云。', 'Core-business Agents cannot run on SaaS, nor be locked to a public cloud.') }}
            </p>
            <p class="oryx-problem-item">
              <strong>{{ t('② 技术栈要对齐', '② Align with the existing stack') }}</strong>
              {{ t('企业现有系统大量是 Java / Spring，Tool 要能直接调现有 Java 服务。', 'Enterprise systems are largely Java / Spring. Tools must call existing Java services directly.') }}
            </p>
            <p class="oryx-solution-line">{{ t('今天的选择只有 Node.js 的 OpenClaw 和 Python 的 Hermes，Java 体系的企业只能跨技术栈写胶水代码。OryxOS 补上 Java 生态在 Agent OS 这一层的缺位。', 'Today the only choices are Node.js OpenClaw and Python Hermes. Java shops must write cross-stack glue code. OryxOS fills the gap in the Java ecosystem at the Agent OS layer.') }}</p>
          </div>
          <div class="oryx-problem-compare">
            <div class="oryx-compare-item oryx-compare-bad">
              <div class="oryx-compare-label">{{ t('今天的做法', 'Today') }}</div>
              <div class="oryx-compare-rows">
                <div class="oryx-compare-row">
                  <span class="oryx-compare-icon">✗</span>
                  <span>{{ t('Node.js / Python 框架，跨技术栈写胶水', 'Node.js / Python stacks, cross-stack glue code') }}</span>
                </div>
                <div class="oryx-compare-row">
                  <span class="oryx-compare-icon">✗</span>
                  <span>{{ t('绑定单个公有云，无法私有化', 'Locked to one public cloud, cannot self-host') }}</span>
                </div>
                <div class="oryx-compare-row">
                  <span class="oryx-compare-icon">✗</span>
                  <span>{{ t('无法纳入现有 IT 治理和审计流程', 'Cannot fit existing IT governance and audit') }}</span>
                </div>
                <div class="oryx-compare-row">
                  <span class="oryx-compare-icon">✗</span>
                  <span>{{ t('每个项目重复搭一层底座', 'Every project rebuilds the base layer') }}</span>
                </div>
              </div>
            </div>
            <div class="oryx-compare-item oryx-compare-good">
              <div class="oryx-compare-label">OryxOS</div>
              <div class="oryx-compare-rows">
                <div class="oryx-compare-row">
                  <span class="oryx-compare-icon oryx-icon-ok">✓</span>
                  <span>{{ t('Java / Spring 原生，与现有系统咬合', 'Java / Spring native, meshes with existing systems') }}</span>
                </div>
                <div class="oryx-compare-row">
                  <span class="oryx-compare-icon oryx-icon-ok">✓</span>
                  <span>{{ t('装在企业自有 K8s / 服务器，数据不出门', 'Runs on your K8s / servers, data stays inside') }}</span>
                </div>
                <div class="oryx-compare-row">
                  <span class="oryx-compare-icon oryx-icon-ok">✓</span>
                  <span>{{ t('全链路审计 day one 落库', 'Full-chain audit persisted from day one') }}</span>
                </div>
                <div class="oryx-compare-row">
                  <span class="oryx-compare-icon oryx-icon-ok">✓</span>
                  <span>{{ t('一个底座承载所有业务 Agent', 'One base hosts every business Agent') }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ── ARCHITECTURE ── -->
    <section class="oryx-section oryx-arch-section">
      <div class="oryx-section-inner">
        <div class="oryx-arch">
          <div class="oryx-arch-layer">
            <div class="oryx-arch-layer-label">{{ t('接入层', 'Access') }}</div>
            <div class="oryx-arch-row">
              <div class="oryx-arch-box">{{ t('CLI Channel', 'CLI Channel') }}</div>
              <div class="oryx-arch-box">{{ t('Web Service (REST API)', 'Web Service (REST API)') }}</div>
            </div>
          </div>
          <div class="oryx-arch-arrow">↕</div>
          <div class="oryx-arch-layer">
            <div class="oryx-arch-layer-label">{{ t('引擎层', 'Engine') }}</div>
            <div class="oryx-arch-row">
              <div class="oryx-arch-box oryx-arch-box-accent">ReActLoop · PromptBuilder · ToolExecutor</div>
            </div>
          </div>
          <div class="oryx-arch-arrow">↕</div>
          <div class="oryx-arch-layer">
            <div class="oryx-arch-layer-label">{{ t('能力层', 'Capabilities') }}</div>
            <div class="oryx-arch-row oryx-arch-row-3">
              <div class="oryx-arch-box">Provider<div class="oryx-arch-sub">{{ t('对接 LLM', 'LLM') }}</div></div>
              <div class="oryx-arch-box">Memory<div class="oryx-arch-sub">{{ t('三层记忆', '3-layer memory') }}</div></div>
              <div class="oryx-arch-box">Tool<div class="oryx-arch-sub">{{ t('内置 + Plugin', 'built-in + plugin') }}</div></div>
            </div>
          </div>
          <div class="oryx-arch-arrow">↕</div>
          <div class="oryx-arch-layer">
            <div class="oryx-arch-layer-label">{{ t('基础层', 'Foundation') }}</div>
            <div class="oryx-arch-box oryx-arch-box-wide">{{ t('Profile / Bootstrap / Skill 加载 · Session 存储 · SQLite · 配置与密钥', 'Profile / Bootstrap / Skill loading · Session store · SQLite · Config & secrets') }}</div>
          </div>
          <div class="oryx-arch-note">
            <span>{{ t('单体应用', 'Monolith') }}</span>
            <span>{{ t('一个引擎 · 一套存储 · 一个进程', 'One engine · one store · one process') }}</span>
            <span>{{ t('装好就跑', 'Ready out of the box') }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- ── CAPABILITIES ── -->
    <section class="oryx-section oryx-primitives-section">
      <div class="oryx-section-inner oryx-primitives-inner">
        <div class="oryx-section-header">
          <div class="oryx-section-tag">{{ t('核心能力', 'Core Capabilities') }}</div>
          <h2 class="oryx-section-title">{{ t('五大能力供养一个引擎', 'Five capabilities feed one engine') }}</h2>
        </div>
        <div class="oryx-primitives">
          <div v-for="p in capabilities" :key="p.title" class="oryx-primitive">
            <div class="oryx-primitive-header">
              <span class="oryx-primitive-icon">{{ p.icon }}</span>
              <div>
                <h3 class="oryx-primitive-title">{{ p.title }}</h3>
                <p class="oryx-primitive-subtitle">{{ p.subtitle }}</p>
              </div>
            </div>
            <pre class="oryx-code"><code>{{ p.code }}</code></pre>
          </div>
        </div>
      </div>
    </section>

    <!-- ── SCENARIOS ── -->
    <section class="oryx-section">
      <div class="oryx-section-inner">
        <div class="oryx-section-header">
          <div class="oryx-section-tag">{{ t('真实场景', 'Real Scenarios') }}</div>
          <h2 class="oryx-section-title">{{ t('一个底座，跑各种业务 Agent', 'One base, every business Agent') }}</h2>
        </div>
        <div class="oryx-scenarios">
          <div v-for="s in scenarios" :key="s.num" class="oryx-scenario">
            <div class="oryx-scenario-num">{{ s.num }}</div>
            <div>
              <h3 class="oryx-scenario-title">{{ s.title }}</h3>
              <p class="oryx-scenario-desc">{{ s.desc }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ── INTEGRATION ── -->
    <section class="oryx-section oryx-sdk-section">
      <div class="oryx-section-inner">
        <div class="oryx-section-header">
          <div class="oryx-section-tag">{{ t('扩展方式', 'Extension') }}</div>
          <h2 class="oryx-section-title">{{ t('Plugin Tool 三档接入，按需选择', 'Three tiers of Plugin Tools — pick what fits') }}</h2>
          <p class="oryx-section-desc">{{ t('选择标准：能用方式一就不用方式二，能用方式二就不用方式三。', 'Rule of thumb: prefer Tier 1 over Tier 2, and Tier 2 over Tier 3.') }}</p>
        </div>
        <div class="oryx-sdk-cards">
          <div class="oryx-sdk-card">
            <div class="oryx-sdk-card-icon">{{ integrations[0].icon }}</div>
            <h3 class="oryx-sdk-card-title">{{ integrations[0].title }}</h3>
            <p class="oryx-sdk-card-desc">{{ integrations[0].desc }}</p>
            <div class="oryx-langs">
              <span v-for="l in integrations[0].langs" :key="l" class="oryx-lang">{{ l }}</span>
            </div>
          </div>
          <div class="oryx-sdk-card oryx-sdk-card-featured">
            <div class="oryx-sdk-card-icon">{{ integrations[1].icon }}</div>
            <h3 class="oryx-sdk-card-title">{{ integrations[1].title }}</h3>
            <p class="oryx-sdk-card-desc">{{ integrations[1].desc }}</p>
            <div class="oryx-sdk-installs">
              <code>oryxos mcp add ./my-server</code>
            </div>
            <div class="oryx-sdk-badges">
              <span class="oryx-sdk-badge">MCP</span>
              <span class="oryx-sdk-badge">stdio / SSE</span>
            </div>
          </div>
          <div class="oryx-sdk-card">
            <div class="oryx-sdk-card-icon">{{ integrations[2].icon }}</div>
            <h3 class="oryx-sdk-card-title">{{ integrations[2].title }}</h3>
            <p class="oryx-sdk-card-desc">{{ integrations[2].desc }}</p>
            <div class="oryx-langs">
              <span v-for="l in integrations[2].langs" :key="l" class="oryx-lang">{{ l }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ── REST API ── -->
    <section class="oryx-section">
      <div class="oryx-section-inner">
        <div class="oryx-section-header">
          <div class="oryx-section-tag">{{ t('Web Service', 'Web Service') }}</div>
          <h2 class="oryx-section-title">{{ t('REST API 暴露所有能力', 'Expose everything via REST') }}</h2>
          <p class="oryx-section-desc">{{ t('业务系统通过 REST API 接入 OryxOS，把 AI 能力嵌入已有产品。', 'Business systems integrate via REST API, embedding AI into existing products.') }}</p>
        </div>
        <div class="oryx-proto-grid">
          <div v-for="g in protocol" :key="g.group" class="oryx-proto-group">
            <div class="oryx-proto-group-label">{{ g.group }}</div>
            <div v-for="r in g.rows" :key="r.sub" class="oryx-proto-row">
              <code class="oryx-proto-subject">{{ r.sub }}</code>
              <span class="oryx-proto-desc">{{ r.desc }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ── CTA ── -->
    <section class="oryx-section oryx-cta-section">
      <div class="oryx-section-inner">
        <div class="oryx-cta">
          <h2 class="oryx-cta-title">{{ t('装好就跑', 'Ready out of the box') }}</h2>
          <p class="oryx-cta-desc">{{ t('克隆、构建、初始化，一个命令开始对话。', 'Clone, build, init — start chatting with one command.') }}</p>
          <pre class="oryx-code oryx-cta-code"><code># 构建 fat JAR
git clone https://gitee.com/robustmq/oryxos.git
cd oryxos && mvn clean package

# 初始化工作区
java -jar oryxos-boot/target/oryxos.jar init

# 开始对话
java -jar oryxos-boot/target/oryxos.jar chat

# 或启动 Web 服务
java -jar oryxos-boot/target/oryxos.jar serve</code></pre>
          <div class="oryx-cta-links">
            <a class="oryx-btn-primary" :href="t('/zh/docs/', '/docs/')">{{ t('查看文档', 'Read the Docs') }}</a>
            <a class="oryx-btn-ghost" href="https://github.com/robustmq/oryxos" target="_blank" rel="noopener">GitHub</a>
          </div>
        </div>
      </div>
    </section>

    <!-- ── FOOTER ── -->
    <footer class="oryx-footer">
      <div class="oryx-footer-inner">
        <div class="oryx-footer-brand">OryxOS</div>
        <div class="oryx-footer-links">
          <a :href="t('/zh/docs/what', '/docs/what')">{{ t('文档', 'Docs') }}</a>
          <a :href="t('/zh/docs/architecture', '/docs/architecture')">{{ t('架构', 'Architecture') }}</a>
          <a href="https://github.com/robustmq/oryxos" target="_blank" rel="noopener">GitHub</a>
          <a href="https://gitee.com/robustmq/oryxos" target="_blank" rel="noopener">Gitee</a>
        </div>
        <div class="oryx-footer-copy">{{ t('企业能完全掌控的、Java 原生的、私有可审计的 Agent 统一底座。', 'A self-owned, Java-native, privately auditable unified Agent base.') }}</div>
      </div>
    </footer>

  </div>
</template>

<style scoped>
.oryx-page {
  min-height: 100vh;
  background: #ffffff;
  color: #000000;
  font-family: inherit;
}

/* ── Hero ── */
.oryx-hero {
  position: relative;
  padding: 100px 24px 80px;
  text-align: center;
  overflow: hidden;
}
.oryx-hero-inner {
  position: relative;
  max-width: 760px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.oryx-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  border-radius: 20px;
  border: 1px solid #d4d4d4;
  background: #f5f5f5;
  color: #555555;
  font-size: 12px;
  margin-bottom: 28px;
}
.oryx-badge-dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  background: #000000;
  animation: pulse 2s infinite;
}
@keyframes pulse {
  0%,100% { opacity:1; transform:scale(1); }
  50% { opacity:0.4; transform:scale(1.4); }
}
.oryx-title {
  margin: 0 0 12px;
  line-height: 1;
}
.oryx-title-name {
  font-size: clamp(56px, 11vw, 104px);
  font-weight: 900;
  letter-spacing: -0.03em;
  color: #000000;
}
.oryx-title-sub {
  font-size: 18px;
  color: #666666;
  margin: 0 0 20px;
}
.oryx-hero-desc {
  font-size: 16px;
  line-height: 1.7;
  color: #444444;
  max-width: 640px;
  margin: 0 0 32px;
}
.oryx-hero-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
  margin-bottom: 20px;
}
.oryx-btn-primary {
  padding: 11px 28px;
  border-radius: 8px;
  background: #000000;
  color: #ffffff;
  font-weight: 600;
  font-size: 14px;
  text-decoration: none;
  transition: opacity 0.2s, transform 0.15s;
}
.oryx-btn-primary:hover { opacity: 0.75; transform: translateY(-1px); }
.oryx-btn-ghost {
  padding: 11px 28px;
  border-radius: 8px;
  border: 1px solid #d4d4d4;
  color: #333333;
  font-weight: 600;
  font-size: 14px;
  text-decoration: none;
  transition: border-color 0.2s, background 0.2s;
}
.oryx-btn-ghost:hover { border-color: #000000; background: #f5f5f5; }
.oryx-hero-note {
  font-size: 12px;
  color: #999999;
}

/* ── Section ── */
.oryx-section { padding: 72px 24px; }
.oryx-section-inner { max-width: 1000px; margin: 0 auto; }
.oryx-primitives-inner { max-width: 1400px; }
.oryx-section-header { text-align: center; margin-bottom: 48px; }
.oryx-section-tag {
  display: inline-block;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #555555;
  padding: 4px 12px;
  border-radius: 20px;
  border: 1px solid #d4d4d4;
  background: #f5f5f5;
  margin-bottom: 14px;
}
.oryx-section-title {
  font-size: clamp(22px, 4vw, 32px);
  font-weight: 700;
  color: #000000;
  margin: 0 0 12px;
}
.oryx-section-desc {
  font-size: 15px;
  color: #666666;
  max-width: 600px;
  margin: 0 auto;
  line-height: 1.6;
}

/* ── Problem ── */
.oryx-problem {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 48px;
  align-items: start;
}
.oryx-problem-text p { color: #666666; line-height: 1.7; margin: 0 0 14px; font-size: 15px; }
.oryx-problem-item strong { color: #000000; display: block; margin-bottom: 4px; }
.oryx-solution-line { color: #000000 !important; font-weight: 600; }
.oryx-problem-compare { display: flex; flex-direction: column; gap: 16px; }
.oryx-compare-item {
  padding: 20px;
  border-radius: 12px;
  border: 1px solid #e5e5e5;
}
.oryx-compare-bad { background: #fafafa; }
.oryx-compare-good { background: #f5f5f5; border-color: #d4d4d4; }
.oryx-compare-label { font-size: 11px; font-weight: 700; color: #999999; margin-bottom: 12px; text-transform: uppercase; letter-spacing: 0.08em; }
.oryx-compare-rows { display: flex; flex-direction: column; gap: 8px; }
.oryx-compare-row { display: flex; align-items: flex-start; gap: 10px; font-size: 13px; color: #555555; line-height: 1.5; }
.oryx-compare-icon { flex-shrink: 0; font-style: normal; color: #bbbbbb; font-weight: 700; width: 14px; }
.oryx-icon-ok { color: #000000; }

/* ── Architecture ── */
.oryx-arch-section { background: #f5f5f5; }
.oryx-arch {
  border: 1px solid #e5e5e5;
  border-radius: 16px;
  background: #ffffff;
  padding: 32px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
.oryx-arch-layer { width: 100%; }
.oryx-arch-layer-label {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #999999;
  margin-bottom: 8px;
  text-align: center;
}
.oryx-arch-row { display: flex; gap: 12px; }
.oryx-arch-row-3 > .oryx-arch-box { flex: 1; }
.oryx-arch-box {
  flex: 1;
  padding: 14px 18px;
  border-radius: 10px;
  border: 1px solid #d4d4d4;
  background: #fafafa;
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  color: #000000;
}
.oryx-arch-box-accent {
  background: #000000;
  border-color: #000000;
  color: #ffffff;
}
.oryx-arch-box-wide { width: 100%; }
.oryx-arch-sub { font-size: 11px; font-weight: 400; color: #999999; margin-top: 2px; }
.oryx-arch-arrow { color: #bbbbbb; font-size: 16px; line-height: 1; }
.oryx-arch-note {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
  justify-content: center;
  margin-top: 16px;
  font-size: 12px;
  color: #999999;
}

/* ── Primitives ── */
.oryx-primitives-section { background: #f5f5f5; }
.oryx-primitives { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); grid-auto-rows: 1fr; gap: 16px; }
.oryx-primitive {
  padding: 20px;
  border-radius: 14px;
  border: 1px solid #e5e5e5;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  gap: 12px;
  transition: border-color 0.2s, box-shadow 0.2s;
  min-width: 0;
  overflow: hidden;
}
.oryx-primitive .oryx-code { flex: 1; }
.oryx-primitive:hover { border-color: #000000; box-shadow: 0 4px 16px rgba(0,0,0,0.06); }
.oryx-primitive-header { display: flex; align-items: flex-start; gap: 12px; }
.oryx-primitive-icon { font-size: 28px; flex-shrink: 0; }
.oryx-primitive-title { font-size: 17px; font-weight: 700; color: #000000; margin: 0 0 2px; }
.oryx-primitive-subtitle { font-size: 12px; color: #999999; margin: 0; }
.oryx-code {
  background: #f5f5f5;
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  padding: 14px 16px;
  font-size: 12px;
  line-height: 1.6;
  color: #333333;
  overflow-x: auto;
  margin: 0;
  white-space: pre;
}
.oryx-code code { font-family: 'JetBrains Mono', 'Fira Code', monospace; background: none; color: inherit; }

/* ── Scenarios ── */
.oryx-scenarios { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; }
.oryx-scenario {
  display: flex;
  gap: 16px;
  padding: 20px;
  border-radius: 12px;
  border: 1px solid #e5e5e5;
  background: #fafafa;
}
.oryx-scenario-num {
  font-size: 28px;
  font-weight: 900;
  color: #e5e5e5;
  line-height: 1;
  flex-shrink: 0;
  font-variant-numeric: tabular-nums;
}
.oryx-scenario-title { font-size: 15px; font-weight: 600; color: #000000; margin: 0 0 6px; }
.oryx-scenario-desc { font-size: 13px; color: #666666; line-height: 1.6; margin: 0; }

/* ── Integration cards ── */
.oryx-sdk-section { background: #f5f5f5; }
.oryx-sdk-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}
.oryx-sdk-card {
  background: #ffffff;
  border: 1px solid #e5e5e5;
  border-radius: 16px;
  padding: 28px 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.oryx-sdk-card-featured { border-color: #000000; }
.oryx-sdk-card-icon { font-size: 28px; }
.oryx-sdk-card-title { font-size: 17px; font-weight: 700; color: #000000; margin: 0; }
.oryx-sdk-card-desc { font-size: 14px; color: #666666; line-height: 1.6; margin: 0; flex: 1; }
.oryx-langs { display: flex; flex-wrap: wrap; gap: 8px; }
.oryx-lang {
  padding: 4px 12px;
  border-radius: 20px;
  border: 1px solid #d4d4d4;
  background: #f5f5f5;
  color: #333333;
  font-size: 12px;
  font-weight: 600;
}
.oryx-sdk-installs { display: flex; flex-direction: column; gap: 6px; }
.oryx-sdk-installs code {
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  font-size: 12px;
  background: #f5f5f5;
  border: 1px solid #e5e5e5;
  border-radius: 6px;
  padding: 5px 10px;
  color: #000000;
  display: block;
}
.oryx-sdk-badges { display: flex; flex-wrap: wrap; gap: 8px; }
.oryx-sdk-badge {
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 700;
  background: #f0f0f0;
  border: 1px solid #d4d4d4;
  color: #333333;
}

/* ── REST API grid ── */
.oryx-proto-grid { display: flex; flex-direction: column; gap: 28px; }
.oryx-proto-group { display: flex; flex-direction: column; gap: 6px; }
.oryx-proto-group-label {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #555555;
  margin-bottom: 4px;
}
.oryx-proto-row {
  display: flex;
  align-items: baseline;
  gap: 16px;
  padding: 8px 14px;
  border-radius: 8px;
  background: #fafafa;
  border: 1px solid #e5e5e5;
  flex-wrap: wrap;
}
.oryx-proto-subject {
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  font-size: 12px;
  color: #000000;
  background: #f0f0f0;
  border: 1px solid #d4d4d4;
  padding: 2px 8px;
  border-radius: 4px;
  flex-shrink: 0;
  white-space: nowrap;
}
.oryx-proto-desc { font-size: 13px; color: #666666; flex: 1; }

/* ── CTA ── */
.oryx-cta-section { background: #f5f5f5; }
.oryx-cta { text-align: center; max-width: 680px; margin: 0 auto; }
.oryx-cta-title { font-size: 28px; font-weight: 700; color: #000000; margin: 0 0 12px; }
.oryx-cta-desc { font-size: 15px; color: #666666; margin: 0 0 24px; }
.oryx-cta-code { text-align: left; margin-bottom: 28px; }
.oryx-cta-links { display: flex; gap: 12px; justify-content: center; flex-wrap: wrap; }

/* ── Footer ── */
.oryx-footer {
  border-top: 1px solid #e5e5e5;
  padding: 40px 24px;
  text-align: center;
}
.oryx-footer-inner { max-width: 1000px; margin: 0 auto; }
.oryx-footer-brand { font-size: 16px; font-weight: 900; letter-spacing: -0.02em; color: #000000; margin-bottom: 12px; }
.oryx-footer-links { display: flex; gap: 20px; justify-content: center; flex-wrap: wrap; margin-bottom: 16px; }
.oryx-footer-links a { font-size: 13px; color: #666666; text-decoration: none; transition: color 0.15s; }
.oryx-footer-links a:hover { color: #000000; }
.oryx-footer-copy { font-size: 12px; color: #999999; }

/* ── Responsive ── */
@media (max-width: 900px) {
  .oryx-sdk-cards { grid-template-columns: 1fr; }
}
@media (max-width: 768px) {
  .oryx-hero { padding: 72px 20px 60px; }
  .oryx-problem { grid-template-columns: 1fr; }
  .oryx-primitives { grid-template-columns: 1fr; }
  .oryx-scenarios { grid-template-columns: 1fr; }
  .oryx-section { padding: 48px 20px; }
}
</style>
