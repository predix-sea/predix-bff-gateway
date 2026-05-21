# Compliance

## Mainland China block (100%)

All requests pass `ComplianceFilter` (except public health/auth nonce):

1. Extract client IP (`X-Forwarded-For` first hop, `CF-Connecting-IP`, `X-Real-IP`)
2. Resolve country via GeoIP2 DB or fallback heuristics
3. If country = `CN` and `predix.compliance.block-mainland-china=true` → **403** `COMPLIANCE_CN_BLOCKED`
4. Audit + metric `bff_compliance_block_total{reason=cn}`

## Country priority (extension)

Configured list (default): `SG, TH, MY, PH, VN, ID`

Used for future routing / feature flags. Policy version: `predix.compliance.policy-version`.

## KYC gate

| Path prefix | KYC required |
|-------------|--------------|
| `/api/v1/markets` | No (read-only browse) |
| `/api/v1/orders` | Yes (`APPROVED`) |
| `/api/v1/custody` | Yes (`APPROVED`) |

Session field `kycStatus` on `SessionUser` — integrate with KYC provider in production.

## Headers

| Header | Purpose |
|--------|---------|
| `X-Forwarded-For` | Client IP chain |
| `X-Country-Code` | Optional override hint (GeoIP takes precedence when resolved) |
| `X-Trace-Id` | Distributed tracing |
