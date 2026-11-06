# post-service

Posts, authenticated feed, likes and comments; admin search/hide/delete.

## 1. Directory tree (packages)

```
src/main/java/com/blog/post/
├── application/
│   └── dto/
│       ├── post/
│       └── request/
├── client/
│   └── dto/
├── config/
├── constant/
├── controller/
├── domain/
│   ├── entity/
│   ├── model/
│   └── valueobject/
├── event/
├── exception/
├── filter/
├── listener/
├── repository/
└── service/
```

## 2. Dependency tree

```
calls ──► post-service :5004
         ├──► user-service :5001          GET /internal/users/{id}/following (feed), role checks
         ├──► media-service :5003         validate/attach media on create & update
         └──► notification-service :5005  POST /internal/notifications/post-created

called by
├── user-service   POST /internal/users/{id}/cleanup (account deletion)
└── clients        /api/v1/posts/** (via gateway)
```

## 3. Packages

### application/dto

<!-- fill: post/ (PostResponse, PageResponse), request/ (Create/Replace/UpdatePostRequest, CreateCommentRequest, HidePostRequest) -->

### client

<!-- fill: clients to user/media/notification services + client/dto shapes -->

### config

<!-- fill: RestClientConfig, ClientProperties, SecurityConfig -->

### constant

<!-- fill: SecurityConstants -->

### controller

<!-- fill: PostController, AdminPostController, InternalUserCleanupController -->

### domain

<!-- fill: Post/Comment/Like entities, CommentModel, visibility/status models -->

### event / listener

<!-- fill: what is published/consumed here (if wired) -->

### exception

<!-- fill: AdminOnlyException, ... -->

### filter

<!-- fill -->

### repository

<!-- fill -->

### service

<!-- fill: PostService, UserCleanupService -->

## 4. Important flows

### Create post → notify followers

<!-- fill: POST /posts (validate media IDs with media-service) → POST /internal/notifications/post-created → async fan-out in notification-service -->

### Feed building

<!-- fill: GET /feed → user-service /internal/users/{id}/following → query posts by those author IDs, cursor pagination -->

### Admin hide/delete

<!-- fill: role claim check on /posts/admin/**; hidden posts filtered from feed -->
