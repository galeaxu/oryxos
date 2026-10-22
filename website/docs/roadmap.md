---
title: Roadmap
description: OryxOS's current state and plans.
outline: deep
---

# Roadmap

OryxOS is moving from usable to great. Below are capabilities by stage.

## Done

| Capability | Notes |
| --- | --- |
| Provider abstraction | on Spring AI Alibaba, many domestic / international models |
| ReAct loop | self-implemented, configurable max iterations |
| Session memory | persisted in SQLite |
| Long-term memory | MEMORY.md, read/written via built-in tools |
| Built-in tools | file / HTTP / shell / memory |
| Plugin Tools | three tiers (Skill / MCP / @Tool) |
| Web Service | REST API |
| CLI Channel | interactive chat |
| Full-chain audit | persisted to SQLite |

## In progress

| Capability | Notes |
| --- | --- |
| Episodic memory | task process state |
| More channels | email, IM, ticketing |
| Profile hot reload | update Agent config at runtime |

## Planned

| Capability | Notes |
| --- | --- |
| RBAC | role-based access control |
| Spring Security integration | reuse existing auth infrastructure |
| Audit export | to enterprise SIEM |
| Cluster mode | horizontal scaling across instances |
| Plugin marketplace | community Skill / MCP distribution |

## Feedback

The roadmap adjusts to real enterprise needs. Reach out with your scenario via GitHub Issues.
