# notification-service

In-app notifications: bell list, unread count, read state; fan-out to followers on new posts.

## 1. Directory tree (packages)

```
src/main/java/com/blog/notification/
├── application/
│   └── dto/
│       ├── request/
│       └── response/
├── client/
│   └── dto/
├── config/
├── constant/
├── controller/
├── domain/
│   ├── entity/
│   └── model/
├── event/
├── exception/
├── filter/
├── listener/
├── repository/
├── security/
└── service/
```

## 2. Dependency tree

```
calls ──► notification-service :5005
         └──► user-service :5001  GET /internal/users/{id}/followers (fan-out recipients)

called by
├── post-service    POST /internal/notifications, /internal/notifications/post-created
├── user-service    POST /internal/users/{id}/cleanup (account deletion)
└── clients         /api/v1/notifications/** (via gateway)
```

## 3. Packages

### application/dto

<!-- fill: request (Create/UpdateNotification..., PostCreatedNotificationRequest), response (NotificationResponse, PageResponse, UnreadCountResponse, UserCleanupResponse) -->

### client

<!-- fill: user-service follower client + client/dto -->

### config

<!-- fill: SecurityConfig, RestClientConfig, async executor if any -->

### constant

<!-- fill -->

### controller

<!-- fill: NotificationController, InternalNotificationController, InternalUserCleanupController -->

### domain

<!-- fill: Notification entity, type/read models -->

### event / listener

<!-- fill: internal event plumbing if used -->

### exception

<!-- fill: GlobalExceptionHandler -->

### filter

<!-- fill -->

### repository

<!-- fill: NotificationRepository (cursor paging, unread count, mark-all-read, cleanup) -->

### security

<!-- fill -->

### service

<!-- fill: NotificationService, PostNotificationService (async fan-out) -->

## 4. Important flows

### Post-created fan-out

<!-- fill: POST /internal/notifications/post-created returns 202 → fetch author's followers from user-service → create notifications asynchronously for each -->

### Read-state management

<!-- fill: per-notification PATCH, read-all PATCH, unread-count GET powering the bell badge -->
