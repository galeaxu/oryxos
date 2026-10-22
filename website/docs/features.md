---
title: Core Capabilities
description: OryxOS's five core capabilities.
outline: deep
---

# Core Capabilities

OryxOS's five capabilities feed the same engine, shared by all Agents.

## 1. LLM Providers (abstraction)

A Provider abstraction built on Spring AI Alibaba; switch models at runtime via Profile, with no code changes.

```yaml
provider:
  name: deepseek
  model: deepseek-chat
  temperature: 0.7
```

Supported models:

| Category | Models |
| --- | --- |
| Domestic (CN) | DeepSeek / Qwen / Ernie / Kimi / Zhipu / Hunyuan / Doubao |
| International | Claude / GPT |

**Key point**: no vendor lock-in. Switching models is a config change, not a code change.

## 2. ReAct loop (Agent brain)

A self-implemented reasoning loop, fully controllable:

```text
Reason  → decide whether to call a tool (LLM thinks)
  ↓
Act     → execute the tool, feed the result back
  ↓
Observe → see the result, decide the next step
  ↓
repeat until the final response
```

- Max iterations default to 10, configurable per Profile.
- Every step is persisted, so the whole reasoning process can be replayed.

## 3. Three-layer memory

| Layer | Content | Storage |
| --- | --- | --- |
| Session | current conversation history | SQLite |
| Long-term | user preferences, project context | MEMORY.md |
| Episodic | task process state | planned |

The Agent reads/writes long-term memory via built-in tools:

```text
save_memory("user prefers Go, project is oryxos")
recall_memory("user preference")
```

After a while, the Agent naturally remembers you.

## 4. Tool system

### Built-in tools

| Tool | Description |
| --- | --- |
| `read_file` / `write_file` / `list_dir` | file operations |
| `http_get` / `http_post` | HTTP requests |
| `shell` | run commands |
| `save_memory` / `recall_memory` | long-term memory |

### Plugin Tools (three tiers)

| Tier | Approach | Best for |
| --- | --- | --- |
| Tier 1 | Write a SKILL.md, reuse MCP servers | General tasks |
| Tier 2 | Write an MCP server in any language | Enterprise systems |
| Tier 3 | Annotate a Java Bean with `@Tool` | Deep Java integration |

See [Plugin Tools](/docs/plugin-tools).

## 5. Web Service (REST API)

All of the above is exposed via REST, directly callable by business systems:

```bash
POST /api/v1/sessions
POST /api/v1/sessions/{id}/messages
POST /api/v1/agents/{name}/invoke
GET  /api/v1/tools
GET  /api/v1/memory
GET  /api/v1/health
```

Full list in [REST API](/docs/rest-api).

## Capability matrix

| Capability | Status |
| --- | --- |
| Provider abstraction (multi-model) | done |
| ReAct loop | done |
| Session memory | done |
| Long-term memory | done |
| Built-in tools | done |
| Plugin Tools (three tiers) | done |
| Web Service | done |
| Episodic memory | planned |
