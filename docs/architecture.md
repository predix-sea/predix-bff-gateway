# Architecture

## Role

`predix-bff-gateway` is the **single entry point** for PrediX frontends. It orchestrates downstream services and enforces policy. It does **not**:

- Hold or move user funds directly
- Settle market outcomes
- Replace custody or matching logic

## Layers

| Layer | Responsibility |
|-------|----------------|
| `controller` | HTTP API, validation |
| `service` | Orchestration, KYC gate, custody guard |
| `client` | WebClient downstream calls (timeout/retry) |
| `security` | SIWE, JWT, session, rate limit |
| `compliance` | Geo IP, CN block, country policy hooks |
| `audit` | Structured audit to PostgreSQL |
| `exception` | Unified error model |

## Data stores

- **Redis**: SIWE nonce (TTL 5m), sessions, rate limiting
- **PostgreSQL**: `audit_log` via Flyway

## Observability

Micrometer metrics:

- `bff_request_total{path,status}`
- `bff_auth_fail_total`
- `bff_compliance_block_total{reason}`
- `bff_downstream_latency_ms{service}`

Actuator: `/actuator/health`, `/actuator/prometheus`
