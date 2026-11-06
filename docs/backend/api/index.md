# API Reference

Backend microservices API documentation. Each service has its own page listing every route and what it is used for.

## Services

| Page | Port | Base path | Authenticated |
|---|---|---|---|
| [api-gateway](api-gateway.md) | 5000 | `/api/v1/**` (proxy) | JWT required except public routes |
| [auth-service](auth-service.md) | 5002 | `/api/v1/auth`, `/.well-known/jwks.json` | Public |
| [user-service](user-service.md) | 5001 | `/api/v1/users` | Yes (except `signup`, `ping`) |
| [media-service](media-service.md) | 5003 | `/api/v1/medias` | Yes (except `ping`) |
| [post-service](post-service.md) | 5004 | `/api/v1/posts` | Yes (except `GET /{postId}` and `GET /{postId}/comments`, optional) |
| [notification-service](notification-service.md) | 5005 | `/api/v1/notifications` | Yes |
| [moderation-service](moderation-service.md) | 5006 | `/api/v1/reports` | Yes (admin re-checked live for moderator actions) |

- The gateway (`http://localhost:5000`) is the only entry point for clients; it validates JWTs, rate-limits by IP and routes by path prefix.
- All `/internal/**` endpoints are service-to-service only and are **not routed** through the gateway.

## Conventions

- **Auth**: all non-public endpoints expect `Authorization: Bearer <JWT>` issued by auth-service; the user identity is the token's `sub` claim.
- **Pagination**: cursor-based — pass the last item's ID as `cursor`, `size` between 1 and 100 (default 20); responses wrap pages in `{ content, nextCursor }`.
- **Media uploads**: `multipart/form-data`; everything else is JSON.
- **Common status codes**: `200/201/202` success with body, `204` delete/no content, `400` validation, `401/403` auth/role, `404` not found.
