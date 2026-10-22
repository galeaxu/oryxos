---
title: Docs
description: OryxOS documentation — a self-owned, Java-native, privately auditable unified Agent base.
---

# OryxOS Documentation

OryxOS is a **unified base** for an Agent operating system. Install it on your own K8s, servers or bare metal, run every business Agent on one base — sharing channels, model routing, memory, tool invocation and audit. Configure Agents and extend tools without writing Agent backend code.

## Where to start

| Goal | Read |
| --- | --- |
| Understand what OryxOS is and who it is for | [What is OryxOS](/docs/what) |
| Learn how engineers extend it | [For Engineers](/docs/for-engineer) |
| Get it running | [Quick Start](/docs/quick-start) |
| Understand the internals | [Architecture](/docs/architecture) |

## Positioning

| Dimension | OryxOS |
| --- | --- |
| Language | Java 21 / Spring Boot 3, native to your Java stack |
| Deployment | Single binary, runs in your own environment, data never leaves |
| Role | The **base (OS)** for Agents, not an Agent app |
| Models | Provider abstraction over many LLMs, switchable at runtime, no vendor lock-in |
| Audit | Full-chain conversation and tool calls persisted to SQLite |
| Tools | Built-in + 3-tier Plugin Tools (no-code / light-code / heavy-code) |

## Core capabilities

1. **LLM Providers (abstraction)** — Spring AI Alibaba, switch models at runtime.
2. **ReAct loop** — a self-implemented, fully controllable Agent brain.
3. **Three-layer memory** — session / long-term / episodic memory.
4. **Tool system** — built-in tools + 3-tier Plugin Tools.
5. **Web Service** — expose everything via REST for seamless integration.
