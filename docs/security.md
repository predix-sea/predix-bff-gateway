# Security

## Authentication

- **SIWE (EIP-4361 style)** wallet login
- Nonce stored in Redis, **one-time use**
- JWT access token + Redis session for revocation

## Authorization

- Spring Security stateless filter chain
- Public: `/api/v1/auth/siwe/nonce`, `/api/v1/auth/siwe/verify`, actuator health, dependency health
- Public read: `GET /api/v1/markets`, `GET /api/v1/markets/{id}`, `GET /api/v1/markets/{id}/orderbook`
- All other `/api/v1/**` require `Authorization: Bearer <token>`

## Input validation

- Jakarta Validation on request DTOs
- Strict wallet address pattern `0x[a-fA-F0-9]{40}`
- JSON bodies validated at controller boundary

## Rate limiting

- Redis sliding window per client IP
- Configurable via `predix.rate-limit.*`

## Fund path isolation

- `CustodyPathGuard` ensures deposit/withdraw/balance only execute inside BACP client context
- Violations emit `CUSTODY_PATH_VIOLATION` and audit event

## Production checklist

- Rotate `JWT_SECRET`
- Enable TLS termination at ingress
- Load MaxMind GeoIP2 Country DB for CN blocking
- Restrict Redis/PostgreSQL network access
- Configure ACLs on downstream services
