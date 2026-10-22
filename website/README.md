# OryxOS Website

The official OryxOS website, built with [VitePress](https://vitepress.dev).

Published to **https://oryxos.robustmq.com** (configured via `public/CNAME`).

## Development

```bash
cd website
npm install
npm run dev
```

## Build

```bash
npm run build
npm run preview
```

Build output goes to `.vitepress/dist`.

## Structure

```text
website/
├── .vitepress/
│   ├── config.mts              # site config (bilingual: / and /zh/)
│   └── theme/
│       ├── index.ts            # register Home component + Layout
│       ├── custom.css          # brand styles (minimal black & white)
│       └── components/
│           ├── Home.vue        # the entire landing page
│           └── Layout.vue      # default layout wrapper
├── index.md                    # English home (layout: home, <Home />)
├── zh/index.md                 # Chinese home
├── docs/                       # English docs
├── zh/docs/                    # Chinese docs
└── public/                     # logo, favicon, CNAME, architecture SVG
```

## Editing content

- **Landing page**: edit `.vitepress/theme/components/Home.vue` (all sections and copy live here; bilingual via the `t(zh, en)` helper).
- **Docs**: edit the Markdown under `docs/` (English) and `zh/docs/` (Chinese).
- **Nav / sidebar**: edit `locales` in `.vitepress/config.mts`.
- **Brand styles**: edit `.vitepress/theme/custom.css`.

## Deployment

The GitHub Actions workflow at `.github/workflows/deploy-website.yml` builds the site and deploys it to GitHub Pages on every push to `main` that touches `website/`. The custom domain is set by `public/CNAME`.
