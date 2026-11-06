# api-gateway

Spring Cloud Gateway (WebFlux). Single entry point for all clients: routing, JWT validation, rate limiting, CORS.

## 1. Directory tree (packages)

```
src/main/java/com/blog/gateway/
├── config/
├── constant/
├── exception/
└── filter/
```

Plus `src/main/resources/application.yml` (routes, JWT, CORS, rate limit) and `ApiGatewayApplication.java`.

## 2. Dependency tree

```
clients (Angular app on :4200)
└── api-gateway :5000
    ├── auth-service :5002          (fetches /.well-known/jwks.json for JWT validation)
    ├── user-service :5001          (/api/v1/users/**)
    ├── media-service :5003         (/api/v1/medias/**)
    ├── post-service :5004          (/api/v1/posts/**)
    ├── notification-service :5005  (/api/v1/notifications/**)
    ├── moderation-service :5006    (/api/v1/reports/**)
    └── Redis                       (RequestRateLimiter token bucket)
```

Depends on: no backend service beyond the JWKS fetch. Nothing internal calls it (it is the edge).

## 3. Packages

### config

<!-- fill: GatewaySecurityProperties, JwtProperties, RateLimitConfig, SecurityConfig -->

### constant

<!-- fill -->

### exception

<!-- fill: GatewayException, GatewayErrorHandler, GatewayErrorCode, GatewayErrorResponse -->

### filter

<!-- fill: RequestIdFilter, RequestLoggingFilter -->

## 4. Important flows

### Request lifecycle through the gateway

<!-- fill: CORS → rate limit (Redis, IP key) → JWT validation (jwks, issuer) except public routes → path routing -->

### Public vs authenticated routes

<!-- fill: /api/v1/users/signup, /api/v1/auth/signin, /actuator/** -->
