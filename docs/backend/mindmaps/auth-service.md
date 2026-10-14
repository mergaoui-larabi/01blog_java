# Auth Service

Login and JWT issuance (`:5002`). It owns the RSA key pair and is the only service allowed to mint tokens; every other service only verifies them.

## Sign-in flow

```mermaid
%%{init: {"theme":"base","themeVariables":{"background":"#ffffff","git0":"#0f172a","gitBranchLabel0":"#f8fafc","cScale0":"#0f172a","cScaleLabel0":"#f8fafc","cScale1":"#334155","cScaleLabel1":"#f8fafc","cScale2":"#334155","cScaleLabel2":"#f8fafc","cScale3":"#334155","cScaleLabel3":"#f8fafc","cScale4":"#334155","cScaleLabel4":"#f8fafc","cScale5":"#334155","cScaleLabel5":"#f8fafc","cScale6":"#334155","cScaleLabel6":"#f8fafc"}}}%%
mindmap
  root((Auth Service :5002))
    "1. Validate request"
      "Blank email or password returns 400"
    "2. Check credentials"
      "POST user-service /internal/users/authenticate"
      "Wrong credentials returns 401"
      "User service down returns 503"
    "3. Sign the token"
      "RSA key from PEM files"
      "RS256, kid blog-auth-key-1"
      "sub, role, 1 hour expiry"
    "4. Respond"
      "200 with a Bearer token"
      "Client sends it on later calls"
```

## Key distribution (JWKS)

```mermaid
%%{init: {"theme":"base","themeVariables":{"background":"#ffffff","git0":"#0f172a","gitBranchLabel0":"#f8fafc","cScale0":"#0f172a","cScaleLabel0":"#f8fafc","cScale1":"#334155","cScaleLabel1":"#f8fafc","cScale2":"#334155","cScaleLabel2":"#f8fafc","cScale3":"#334155","cScaleLabel3":"#f8fafc","cScale4":"#334155","cScaleLabel4":"#f8fafc","cScale5":"#334155","cScaleLabel5":"#f8fafc","cScale6":"#334155","cScaleLabel6":"#f8fafc"}}}%%
mindmap
  root((JWKS endpoint))
    "GET /.well-known/jwks.json"
      "Public, no token needed"
    "Publishes the RSA public key"
      "kid blog-auth-key-1, RS256"
    "Consumed by"
      "API gateway"
      "user-service"
      "media-service"
```

**Liveness:** `GET /api/v1/auth/ping` returns `pong`.
