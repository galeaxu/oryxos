---
title: Security & Audit
description: OryxOS's private deployment, key management and full-chain audit.
outline: deep
---

# Security & Audit

OryxOS is designed for regulated industries: security and audit are base capabilities, not add-ons.

## Private deployment

OryxOS is a single binary, installed in your own environment:

- K8s, VMs or bare metal.
- Data (sessions, memory, keys) stays entirely on your internal network.
- No data is sent back to any external service.

## Key management

Provider API keys are **never written in plaintext**; they are injected from environment variables:

```bash
export DEEPSEEK_API_KEY=sk-xxxxxx
```

- Keys are not written into Profiles, the database or logs.
- Supported via your secret-management service (e.g. Vault) injecting environment variables.

## Full-chain audit

Every interaction is persisted to SQLite, queryable and auditable:

| Audit item | Content |
| --- | --- |
| Session | who started a session, when |
| Message | full text of user input and Agent response |
| Model call | which Provider / model, latency, token usage |
| Tool call | which Tool, arguments, result |
| Memory I/O | long-term memory reads and writes |

Audit records are queryable via the REST API, or read directly from the SQLite file:

```bash
curl http://localhost:8080/api/v1/sessions/{id}
```

## Tool execution boundaries

- **Built-in tools** limit file, command and HTTP operations to the workspace.
- **Plugin Tools** run as separate processes (MCP), with permissions aligned to existing systems.
- High-risk operations (such as `shell`) can be turned off per Agent in the Profile.

```yaml
tools:
  - read_file
  - http_get
  # shell disabled
```

## Data sovereignty

| Concern | OryxOS approach |
| --- | --- |
| Data location | your own environment, never leaves |
| Model choice | can use self-hosted models |
| Audit retention | entirely in your own SQLite |
| No external dependency | single binary, no external SaaS |

## Roadmap

- Role-based access control (RBAC)
- Deep integration with Spring Security
- Audit-log export to enterprise SIEM

See [Roadmap](/docs/roadmap).
