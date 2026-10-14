# User Service

Owns user accounts (`:5001`, PostgreSQL + Flyway). Exposes the public signup API, a private credential-checking API used by the auth service, and a "who am I" endpoint.

## Signup (public)

```mermaid
%%{init: {"theme":"base","themeVariables":{"background":"#ffffff","git0":"#0f172a","gitBranchLabel0":"#f8fafc","cScale0":"#0f172a","cScaleLabel0":"#f8fafc","cScale1":"#334155","cScaleLabel1":"#f8fafc","cScale2":"#334155","cScaleLabel2":"#f8fafc","cScale3":"#334155","cScaleLabel3":"#f8fafc","cScale4":"#334155","cScaleLabel4":"#f8fafc","cScale5":"#334155","cScaleLabel5":"#f8fafc","cScale6":"#334155","cScaleLabel6":"#f8fafc"}}}%%
mindmap
  root((Signup))
    "POST /api/v1/users/signup"
    "1. Validate input"
      "Email, username, password, age, gender"
    "2. Uniqueness"
      "Email taken returns 409"
      "Username taken returns 409"
    "3. Save"
      "BCrypt hash, role USER"
      "201 without the password hash"
```

## Credential check (internal only)

```mermaid
%%{init: {"theme":"base","themeVariables":{"background":"#ffffff","git0":"#0f172a","gitBranchLabel0":"#f8fafc","cScale0":"#0f172a","cScaleLabel0":"#f8fafc","cScale1":"#334155","cScaleLabel1":"#f8fafc","cScale2":"#334155","cScaleLabel2":"#f8fafc","cScale3":"#334155","cScaleLabel3":"#f8fafc","cScale4":"#334155","cScaleLabel4":"#f8fafc","cScale5":"#334155","cScaleLabel5":"#f8fafc","cScale6":"#334155","cScaleLabel6":"#f8fafc"}}}%%
mindmap
  root((Credential check))
    "Called by auth-service"
      "POST /internal/users/authenticate"
    "1. Find by email"
      "Unknown email returns 401"
    "2. Compare password"
      "Mismatch returns 401"
    "3. Answer"
      "userId and role, never the hash"
```

## Security

```mermaid
%%{init: {"theme":"base","themeVariables":{"background":"#ffffff","git0":"#0f172a","gitBranchLabel0":"#f8fafc","cScale0":"#0f172a","cScaleLabel0":"#f8fafc","cScale1":"#334155","cScaleLabel1":"#f8fafc","cScale2":"#334155","cScaleLabel2":"#f8fafc","cScale3":"#334155","cScaleLabel3":"#f8fafc","cScale4":"#334155","cScaleLabel4":"#f8fafc","cScale5":"#334155","cScaleLabel5":"#f8fafc","cScale6":"#334155","cScaleLabel6":"#f8fafc"}}}%%
mindmap
  root((Security))
    "OAuth2 resource server"
      "Keys from auth-service JWKS"
    "Public paths"
      "/api/v1/users/signup"
      "/internal/users/authenticate"
      "/actuator/health"
    "Everything else needs a valid JWT"
```

## Notes

- `GET /api/v1/users/me` returns the `userId` from the JWT `sub` claim; `GET /api/v1/users/ping` returns `pong`.
- Flyway migration `V1` creates `users` (unique `email` and `username`, `password_hash`, `age`, `gender`, `role`; `avatar_media_id` reserved for the media service).
