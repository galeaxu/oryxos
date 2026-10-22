---
title: Quick Start
description: Get OryxOS running in five minutes.
outline: deep
---

# Quick Start

## Requirements

| Dependency | Version |
| --- | --- |
| JDK | 21+ |
| Maven | 3.9+ |

## Build

```bash
git clone https://gitee.com/robustmq/oryxos.git
cd oryxos
mvn clean package
```

The build produces a fat JAR: `oryxos-boot/target/oryxos.jar`.

## Init

```bash
java -jar oryxos-boot/target/oryxos.jar init
```

`init` creates the OryxOS workspace in the current directory:

```text
.oryxos/
├── profiles/      # Agent configuration
├── skills/        # Skill definitions
├── memory/        # long-term memory (MEMORY.md)
└── oryxos.db      # session store (SQLite)
```

## Configure a model

Set the Provider key, injected from the environment:

```bash
export DEEPSEEK_API_KEY=sk-xxxxxx
```

## Start chatting

### CLI mode

```bash
java -jar oryxos-boot/target/oryxos.jar chat
```

Enter interactive chat; the Agent calls tools automatically when needed.

### Web service mode

```bash
java -jar oryxos-boot/target/oryxos.jar serve
```

Listens on `http://localhost:8080` by default; business systems integrate via the [REST API](/docs/rest-api).

```bash
curl http://localhost:8080/api/v1/health
```

## Switch models

Change the Provider in the Profile — no code changes:

```yaml
provider:
  name: kimi
  model: moonshot-v1-8k
```

Supports DeepSeek / Qwen / Ernie / Kimi / Zhipu / Hunyuan / Doubao / Claude / GPT.

## Next

- [Architecture](/docs/architecture) — understand the internals
- [Plugin Tools](/docs/plugin-tools) — three tiers of tool extension
- [REST API](/docs/rest-api) — integrate business systems
