# API gateway

Part of the [API Reference](api.md). Single entry point for all clients (e.g. the Angular app on `http://localhost:4200`): routing, JWT validation, rate limiting, CORS.

Port: `5000` — clients call services **through** the gateway; the routes below are what it exposes.

## Routes

| Path predicate | Upstream |
|---|---|
| `/api/v1/users/**` | `http://localhost:5001` (user-service) |
| `/api/v1/auth/**` | `http://localhost:5002` (auth-service) |
| `/api/v1/medias/**` | `http://localhost:5003` (media-service) |
| `/api/v1/posts/**` | `http://localhost:5004` (post-service) |
| `/api/v1/notifications/**` | `http://localhost:5005` (notification-service) |
| `/api/v1/reports/**` | `http://localhost:5006` (moderation-service) |

## Behavior

- Validates every JWT against auth-service (`/.well-known/jwks.json`, expected issuer `blog-auth-service`) before forwarding. Public (no JWT) routes: `/api/v1/users/signup`, `/api/v1/auth/signin`, `/actuator/**`.
- Applies a global Redis rate limit (20 req/s replenish, burst 200, keyed by client IP) and CORS for `http://localhost:4200`.
- Adds request IDs and request logging (`RequestIdFilter`, `RequestLoggingFilter`), and normalizes errors through `GatewayErrorHandler`.
- **Not routed**: all `/internal/**` endpoints are service-to-service only and intentionally absent from the routing table.

## Endpoints

### `{host:port}/api/v1/**`

- **Used for**: proxy to all service APIs documented in [auth-service](auth-service.md), [user-service](user-service.md), [media-service](media-service.md), [post-service](post-service.md), [notification-service](notification-service.md), [moderation-service](moderation-service.md).

### `/actuator/**`

- **Used for**: gateway health/info/metrics/prometheus endpoints (public).
