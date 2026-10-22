---
title: Architecture
description: OryxOS's four-layer architecture and module layout.
outline: deep
---

# Architecture

OryxOS is a **monolith**: one process, one store, one engine. Ready out of the box, with no extra middleware.

![OryxOS four-layer architecture](/diagram-architecture.svg)

## Four layers

### Access

The external entry points. Two today:

- **CLI Channel** — interactive command line, for developers and operators.
- **Web Service** — REST API, for business-system integration.

### Engine

The Agent's brain:

- **ReActLoop** — the Reason → Act → Observe loop.
- **PromptBuilder** — assembles system prompt, history and tool descriptions.
- **ToolExecutor** — executes tool calls and feeds results back.

### Capabilities

Three capabilities shared by all Agents:

- **Provider** — LLM access, abstracted on Spring AI Alibaba.
- **Memory** — three layers (session / long-term / episodic).
- **Tool** — built-in tools + Plugin Tools.

### Foundation

The infrastructure underpinning everything above:

- **Profile / Bootstrap / Skill loading** — assembles Agents at startup.
- **Session store** — conversation history, SQLite.
- **SQLite** — single-file storage, zero operations.
- **Config & secrets** — centrally managed, keys injected from the environment.

## Request lifecycle

The path of one user message through OryxOS:

```text
User message
  ↓
Access layer (CLI / REST)
  ↓
Engine: PromptBuilder assembles context
  ↓
ReActLoop:
  Reason → LLM thinks
    ├─ no  → return the final response
    └─ yes → ToolExecutor runs the tool → result fed back → Reason again
  ↓
Access layer returns the response
  ↓
Persisted throughout (SQLite) — auditable
```

## Module layout (Maven multi-module)

| Module | Responsibility |
| --- | --- |
| `oryxos-boot` | Startup entry, fat JAR packaging, CLI |
| `oryxos-core` | Engine, ReAct loop, tool execution |
| `oryxos-provider` | LLM Provider abstraction and implementations |
| `oryxos-memory` | Three-layer memory |
| `oryxos-tools` | Built-in tools and Plugin Tool loading |
| `oryxos-web` | Web Service (REST API) |

## Technology choices

| Component | Choice | Rationale |
| --- | --- | --- |
| Language | Java 21 | Consistent with existing enterprise stacks; virtual threads and modern features |
| Framework | Spring Boot 3 | The de facto standard for enterprise Java |
| Model access | Spring AI Alibaba | Unifies many domestic and international models |
| Storage | SQLite | Single-file, zero operations, shippable with the binary |
| Extension protocol | MCP | Rich community ecosystem, cross-language |
| Deployment | Single binary | Matches enterprise private-delivery habits |
