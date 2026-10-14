# API Gateway

Single entry point of the backend (`:5000`, Spring Cloud Gateway / WebFlux). Every client request crosses it before reaching a microservice, and every downstream failure is translated into a uniform JSON error.

## Request pipeline

```mermaid
%%{init: {"theme":"base","themeVariables":{"background":"#ffffff","git0":"#0f172a","gitBranchLabel0":"#f8fafc","cScale0":"#0f172a","cScaleLabel0":"#f8fafc","cScale1":"#334155","cScaleLabel1":"#f8fafc","cScale2":"#334155","cScaleLabel2":"#f8fafc","cScale3":"#334155","cScaleLabel3":"#f8fafc","cScale4":"#334155","cScaleLabel4":"#f8fafc","cScale5":"#334155","cScaleLabel5":"#f8fafc","cScale6":"#334155","cScaleLabel6":"#f8fafc"}}}%%
mindmap
  root((API Gateway :5000))
    "1. CORS"
      "Only localhost:4200"
      "Credentials allowed"
    "2. Request ID"
      "UUID on request and response"
    "3. Authentication"
      "Public paths skip the JWT"
        "/api/v1/users/signup"
        "/api/v1/auth/**"
        "/actuator/**"
      "Everything else needs a valid JWT"
        "Keys from auth-service JWKS"
    "4. Rate limiting"
      "10 req/s per IP, burst 50, Redis"
      "Over quota returns 429"
    "5. Routing"
      "/api/v1/users/** goes to user-service :5001"
      "/api/v1/auth/** goes to auth-service :5002"
```

## Error responses

Every failure comes back as the same JSON body: `timestamp, status, code, message, path, requestId`.

```mermaid
%%{init: {"theme":"base","themeVariables":{"background":"#ffffff","git0":"#0f172a","gitBranchLabel0":"#f8fafc","cScale0":"#0f172a","cScaleLabel0":"#f8fafc","cScale1":"#334155","cScaleLabel1":"#f8fafc","cScale2":"#334155","cScaleLabel2":"#f8fafc","cScale3":"#334155","cScaleLabel3":"#f8fafc","cScale4":"#334155","cScaleLabel4":"#f8fafc","cScale5":"#334155","cScaleLabel5":"#f8fafc","cScale6":"#334155","cScaleLabel6":"#f8fafc"}}}%%
mindmap
  root((Error))
    "404 no matching route"
    "401 invalid or missing token"
    "403 forbidden"
    "503 service unreachable"
    "504 gateway timeout"
    "500 anything else"
```
