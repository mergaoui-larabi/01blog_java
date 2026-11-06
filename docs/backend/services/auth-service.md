# auth-service

Issues signed JWTs on signin and publishes the public signing key (JWKS) for all resource services.

## 1. Directory tree (packages)

```
src/main/java/com/blog/auth/
├── application/
│   ├── client/
│   └── dto/
│       ├── error/
│       ├── request/
│       ├── response/
│       └── user/
├── config/
├── constant/
├── controller/
├── exception/
├── filter/
├── security/
└── service/
```

## 2. Dependency tree

```
calls ──► auth-service :5002
         ├──► user-service :5001  POST /internal/users/authenticate (verify credentials)
         └──► (its own RSA key pair) signs JWTs

called by
├── api-gateway + every resource service  GET /.well-known/jwks.json
└── clients                               POST /api/v1/auth/signin (via gateway)
```

## 3. Packages

### application/client

<!-- fill: UserServiceClientConfig — REST client to user-service -->

### application/dto

<!-- fill: LoginRequest, LoginResponse, user/ + error/ DTOs -->

### config

<!-- fill -->

### constant

<!-- fill: SecurityConstants -->

### controller

<!-- fill: AuthController (signin, ping), JwksController -->

### exception

<!-- fill -->

### filter

<!-- fill -->

### security

<!-- fill: JwksService, key generation/loading, token issuing -->

### service

<!-- fill: AuthenticationService -->

## 4. Important flows

### Signin

<!-- fill: client → gateway (public route) → auth-service → user-service /internal/users/authenticate → signed JWT + role returned -->

### Token validation support (JWKS)

<!-- fill: how the key pair is created/loaded and exposed so other services validate offline -->
