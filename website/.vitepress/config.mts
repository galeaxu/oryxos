import { defineConfig } from 'vitepress'

export default defineConfig({
  title: 'OryxOS',
  titleTemplate: ':title — OryxOS',
  description: '企业能完全掌控的、Java 原生的、私有可审计的 Agent 统一底座。',
  base: '/',
  cleanUrls: true,
  appearance: 'force-light',
  srcExclude: ['README.md', '**/README.md'],

  head: [
    ['link', { rel: 'icon', type: 'image/svg+xml', href: '/favicon.svg' }],
    ['link', { rel: 'preconnect', href: 'https://fonts.googleapis.com' }],
    ['link', { rel: 'preconnect', href: 'https://fonts.gstatic.com', crossorigin: '' }],
    ['link', { rel: 'stylesheet', href: 'https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@700&display=swap' }],
    ['meta', { name: 'author', content: 'OryxOS' }],
    ['meta', { name: 'keywords', content: 'OryxOS, Agent OS, Java Agent framework, Spring AI, ReAct, LLM, private deployment, enterprise AI, agent platform, self-hosted, MCP' }],
    ['meta', { name: 'robots', content: 'index, follow' }],
    ['meta', { property: 'og:type', content: 'website' }],
    ['meta', { property: 'og:site_name', content: 'OryxOS' }],
    ['meta', { property: 'og:title', content: 'OryxOS — 企业自有的 Java 原生 Agent 统一底座' }],
    ['meta', { property: 'og:description', content: '企业能完全掌控的、Java 原生的、私有可审计的 Agent 统一底座。五大核心能力：Provider 抽象、ReAct 循环、三层记忆、Tool 体系、Web Service。' }],
    ['meta', { property: 'og:url', content: 'https://oryxos.robustmq.com' }],
    ['meta', { name: 'twitter:card', content: 'summary_large_image' }],
    ['meta', { name: 'twitter:title', content: 'OryxOS — 企业自有的 Java 原生 Agent 统一底座' }],
    ['meta', { name: 'twitter:description', content: '企业能完全掌控的、Java 原生的、私有可审计的 Agent 统一底座。' }],
    ['link', { rel: 'canonical', href: 'https://oryxos.robustmq.com' }],
  ],

  locales: {
    root: {
      label: 'English',
      lang: 'en-US',
      themeConfig: {
        nav: [
          { text: 'Home', link: '/' },
          { text: 'Docs', link: '/docs/what' },
          { text: 'GitHub', link: 'https://github.com/robustmq/oryxos' },
          { text: 'Gitee', link: 'https://gitee.com/robustmq/oryxos' },
        ],
        sidebar: {
          '/docs/': [
            {
              text: 'Getting Started',
              items: [
                { text: 'What is OryxOS', link: '/docs/what' },
                { text: 'For Engineers', link: '/docs/for-engineer' },
                { text: 'Quick Start', link: '/docs/quick-start' },
              ],
            },
            {
              text: 'Deep Dives',
              items: [
                { text: 'Architecture', link: '/docs/architecture' },
                { text: 'Core Capabilities', link: '/docs/features' },
                { text: 'Plugin Tools', link: '/docs/plugin-tools' },
                { text: 'Security & Audit', link: '/docs/security' },
              ],
            },
            {
              text: 'Reference',
              items: [
                { text: 'REST API', link: '/docs/rest-api' },
                { text: 'Roadmap', link: '/docs/roadmap' },
              ],
            },
          ],
        },
      },
    },
    zh: {
      label: '中文',
      lang: 'zh-CN',
      link: '/zh/',
      themeConfig: {
        nav: [
          { text: '首页', link: '/zh/' },
          { text: '文档', link: '/zh/docs/what' },
          { text: 'GitHub', link: 'https://github.com/robustmq/oryxos' },
          { text: 'Gitee', link: 'https://gitee.com/robustmq/oryxos' },
        ],
        sidebar: {
          '/zh/docs/': [
            {
              text: '快速入门',
              items: [
                { text: 'OryxOS 是什么', link: '/zh/docs/what' },
                { text: '给工程师', link: '/zh/docs/for-engineer' },
                { text: '快速开始', link: '/zh/docs/quick-start' },
              ],
            },
            {
              text: '深入了解',
              items: [
                { text: '系统架构', link: '/zh/docs/architecture' },
                { text: '核心能力', link: '/zh/docs/features' },
                { text: 'Plugin Tool', link: '/zh/docs/plugin-tools' },
                { text: '安全与审计', link: '/zh/docs/security' },
              ],
            },
            {
              text: '参考',
              items: [
                { text: 'REST API', link: '/zh/docs/rest-api' },
                { text: '路线图', link: '/zh/docs/roadmap' },
              ],
            },
          ],
        },
      },
    },
  },

  themeConfig: {
    siteTitle: false,
    logo: '/logo.svg',
    socialLinks: [
      { icon: 'github', link: 'https://github.com/robustmq/oryxos' },
    ],
  },

  sitemap: {
    hostname: 'https://oryxos.robustmq.com',
  },
})

