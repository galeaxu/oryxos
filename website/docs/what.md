---
title: What is OryxOS
description: OryxOS — a self-owned, Java-native, privately auditable unified Agent base.
outline: deep
---

# What is OryxOS

OryxOS is a **unified base for an Agent operating system**.

Install it on your own K8s, servers or bare metal, run every business Agent on one base. All Agents share one set of channels, model routing, memory, tool invocation and audit. Your job becomes **configuring Agents and extending tools** — not writing Agent backend code.

## Why OryxOS exists

Regulated industries (finance, energy, state-owned enterprises) face hard constraints when putting Agents into production:

- **Data sovereignty**: core-business Agents cannot run on SaaS, nor be locked to a public cloud.
- **Tech stack**: enterprise systems are largely Java / Spring; tools must call existing Java services directly.
- **Audit**: every model call, tool call and data access must be traceable and auditable.
- **Controllable cost**: self-hostable, without paying an external vendor per token.

Today the main choices are Node.js OpenClaw and Python Hermes — excellent tools, but Java shops must either write cross-stack glue code or give up.

**OryxOS fills the gap in the Java ecosystem at the Agent OS layer.**

## Two core problems

**Problem one: how do Agents connect to models and data?**

Every enterprise has its own systems, its own credentials, its own model preferences. Without a unified base, every Agent project re-solves "how to call an LLM, where keys live, where memory is stored, how tools plug in".

**Problem two: how does the enterprise govern these Agents?**

Agents can call tools, read/write data and make outbound requests. The enterprise must know: who, when, with which model, called which tool, accessed which data. Without a unified base, this is scattered across projects and ungovernable.

**OryxOS solves both in one base.**

## How OryxOS solves it

### One base, every Agent

OryxOS is a monolith: one process holds the engine, capabilities and storage. Ready out of the box — no extra middleware to operate.

![OryxOS four-layer architecture](/diagram-architecture.svg)

### Five core capabilities

| Capability | Detail |
| --- | --- |
| LLM Providers | Provider abstraction on Spring AI Alibaba, runtime model switch, no vendor lock-in |
| ReAct loop | Self-implemented Agent brain, Reason → Act → Observe, configurable max iterations |
| Three-layer memory | Session (SQLite) / long-term (MEMORY.md) / episodic (planned) |
| Tool system | Built-in tools + 3-tier Plugin Tools (no-code Skill / light-code MCP / heavy-code @Tool) |
| Web Service | REST API exposing sessions, agents, profiles, tools and more |

### Three tiers of tool extension

The rule of thumb is simple: **prefer Tier 1 over Tier 2, and Tier 2 over Tier 3.**

| Tier | Approach | Best for |
| --- | --- | --- |
| Tier 1 (no-code) | Write a SKILL.md, reuse community MCP servers | Most general tasks |
| Tier 2 (light-code) | Write an MCP server in any language, expose internal systems | Enterprise system integration |
| Tier 3 (heavy-code) | Annotate a Java Bean with `@Tool`, reuse existing Spring Beans | Deep Java integration |

## Positioning

OryxOS **is the base, not an Agent app**. It provides the layer every Agent shares, so business teams focus on their own logic.

| | **OryxOS** | LangChain / Spring AI directly | Commercial SaaS Agent platforms |
| --- | --- | --- | --- |
| Self-hosted | Single binary, in your environment | You build a layer yourself | Usually unsupported |
| Java-native | Yes, meshes with Spring | Depends on stack | Usually not Java |
| Unified base | One process hosts all Agents | Every project reinvents it | Supported but closed |
| Full-chain audit | Persisted from day one | You implement it | Black box |
| Swappable models | Provider abstraction, runtime switch | Needs rework | Usually locked in |

**OryxOS is not:**
- a general chatbot framework — it is a base hosting many business Agents
- an orchestration framework — it runs Agents, it does not make business decisions for them
- a data pipeline or event bus — it is oriented around Agent conversations and tool calls

## Core concepts

### Profile

The configuration unit of one Agent. It declares which Provider, which system prompt, which tools, how memory is configured. Business teams define their Agents via Profiles.

### Provider

The abstraction for model access. One Provider represents one available LLM (DeepSeek, Qwen, Kimi, Zhipu, Claude, GPT…). Switchable at runtime via Profile, without code changes.

### ReAct loop

The Agent's reasoning brain. After a user message: **Reason (LLM decides whether to call a tool) → Act (execute tool) → Observe (see the result) → loop** until the final response.

### Three-layer memory

- **Session memory**: current conversation history, persisted in SQLite.
- **Long-term memory**: cross-session facts such as user preferences and project context, stored in MEMORY.md, read/written by the Agent via built-in tools.
- **Episodic memory**: task process state, planned.

### Plugin Tool

OryxOS's extension mechanism. Three tiers: no-code Skill, light-code MCP server, heavy-code `@Tool` Java Bean.

## Design principles

**Base, not framework.** OryxOS captures the layer every Agent needs, so business teams only write their own logic.

**Java-native.** Meshes directly with the existing Spring / Java ecosystem, without adding a second runtime.

**Single binary.** One process, one store, ready out of the box — matching existing enterprise deployment and operations habits.

**Swappable models.** A Provider abstraction over many models, switchable at runtime, avoiding vendor lock-in.

**Audit first.** Every conversation and tool call is persisted, meeting the traceability requirements of regulated industries.

## Next

- [For Engineers](/docs/for-engineer) — understand how to extend OryxOS
- [Quick Start](/docs/quick-start) — running in five minutes
- [Architecture](/docs/architecture) — see the internals
