# notification-service API

Part of the [API Reference](api.md). In-app notifications (new post from someone you follow, etc.).

Base: `http://localhost:5005` — routed via gateway at `/api/v1/notifications/**`. Authenticated.

## GET `/api/v1/notifications?cursor=&unreadOnly=&size=`

- **Used for**: the notification bell list — the caller's notifications, newest first, cursor-paginated; `unreadOnly=true` filters to unread.

## GET `/api/v1/notifications/unread-count`

- **Used for**: the badge count on the notification icon. Returns `{ "count": number }`.

## PATCH `/api/v1/notifications/read-all`

- **Used for**: marking every one of the caller's notifications as read ("mark all read"). Returns `204`.

## PATCH `/api/v1/notifications/{notificationId}`

- **Used for**: toggling the read state of one notification. Body `{ "read": boolean }`.

## DELETE `/api/v1/notifications/{notificationId}`

- **Used for**: deleting one of the caller's notifications. Returns `204`.

## Internal (other services publish events here; not routed through the gateway)

- POST `/internal/notifications` — **Used for**: bulk-creating notifications for given recipients. Body `{ "notifications": [ ... ] }` (1–100 items).
- POST `/internal/notifications/post-created` — **Used for**: post-service announcing a new post; fans it out asynchronously to the author's followers. Body `{ "postId": UUID, "authorId": UUID }`, returns `202`.
- POST `/internal/users/{userId}/cleanup` — **Used for**: user-service deleting all notifications of an account being removed.
