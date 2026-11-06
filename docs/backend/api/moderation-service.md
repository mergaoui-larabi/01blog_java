# moderation-service API

Part of the [API Reference](api.md). User reports (posts and users) and the admin workflow for resolving them.

Base: `http://localhost:5006` — routed via gateway at `/api/v1/reports/**`. Authenticated; admin actions re-check the role live.

## GET `/api/v1/reports?status=&cursor=&size=`

- **Used for**: admin queue of reports. Administrator only: the caller's **current** role is re-checked live against user-service (`GET /internal/users/{userId}/role`) on every request, so the JWT `role` claim alone is not trusted.
- **Query params**: `status` (`OPEN`, `UNDER_REVIEW`, `RESOLVED`, `DISMISSED`, optional), `cursor` (ID of last report from the previous page, omitted on first call), `size` (1–100, default 20).

## POST `/api/v1/reports/posts`

- **Used for**: reporting a post. Any authenticated user.
- **Body**: `{ "postId": UUID, "reason": string (≤2000 chars) }`. Returns `201` with the `ReportResponse`.

## POST `/api/v1/reports/users`

- **Used for**: reporting a user. Any authenticated user.
- **Body**: `{ "userId": UUID, "reason": string (≤2000 chars) }`. Returns `201` with the `ReportResponse`.

## PATCH `/api/v1/reports/{reportId}`

- **Used for**: admin moving a report through its lifecycle. Body `{ "status": "OPEN" | "UNDER_REVIEW" | "RESOLVED" | "DISMISSED" }`. The acting admin is recorded on the report. Returns `200`.

## DELETE `/api/v1/reports/{reportId}`

- **Used for**: admin deleting a report outright. Returns `204`.

## Internal

- POST `/internal/users/{userId}/cleanup` — **Used for**: user-service purging the reports of an account being deleted. Not routed through the gateway.
