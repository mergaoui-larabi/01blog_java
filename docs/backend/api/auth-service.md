# auth-service API

Part of the [API Reference](api.md). Issues JWTs for login and publishes the signing key so other services can verify tokens offline.

Base: `http://localhost:5002` — routed via gateway at `/api/v1/auth/**`; the JWKS path is not routed (services call it directly).

## GET `/.well-known/jwks.json`

- **Used for**: JWKS endpoint. Serves the public RSA key so every resource service (and the gateway) can validate the signature of JWTs it receives. No auth required.

## GET `/api/v1/auth/ping`

- **Used for**: health probe — returns `pong`.

## POST `/api/v1/auth/signin`

- **Used for**: logging in. The client submits credentials and receives a signed JWT plus the user's role, which the frontend attaches as `Authorization: Bearer <token>` to every subsequent call.
- **Body**: `{ "email": string, "password": string }`
- **Returns**: `200` with the `LoginResponse` (token, expiry, user info/role). Internally the service verifies the password by calling user-service `POST /internal/users/authenticate`.
