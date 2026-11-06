# user-service

Owns accounts, profiles, the follow graph, bans and account-deletion orchestration.

## 1. Directory tree (packages)

```
src/main/java/com/blog/user/
├── application/
│   └── dto/
│       ├── error/
│       ├── request/
│       └── response/
├── client/
├── config/
├── constant/
├── controller/
├── domain/
│   ├── entity/
│   ├── model/
│   └── valueobject/
├── exception/
├── filter/
├── repository/
└── service/
```

## 2. Dependency tree

```
calls ──► user-service :5001
         ├──► post-service :5004          POST /internal/users/{id}/cleanup
         ├──► notification-service :5005  POST /internal/users/{id}/cleanup
         └──► moderation-service :5006    POST /internal/users/{id}/cleanup

called by
├── auth-service        POST /internal/users/authenticate
├── post-service        GET /internal/users/{id}/following
├── notification-service GET /internal/users/{id}/followers
├── moderation-service  GET /internal/users/{id}/role
└── clients             /api/v1/users/** (via gateway)
```

## 3. Packages

### application/dto

<!-- fill: CreateUserRequest, UpdateUserAvatarRequest, PageResponse, UserResponse, UserSearchResponse, UsernameSearchResponse, AccountDeletionResponse, ... -->

### client

<!-- fill: cleanup clients to post/notification/moderation services -->

### config

<!-- fill -->

### constant

<!-- fill -->

### controller

<!-- fill: UserController, InternalUserController -->

### domain

<!-- fill: entity (User, follow edges?), model (AccountStatus...), valueobject (Username, Email...) -->

### exception

<!-- fill -->

### filter

<!-- fill -->

### repository

<!-- fill -->

### service

<!-- fill: UserService, FollowerService, UserDeletionService -->

## 4. Important flows

### Account deletion orchestration

<!-- fill: DELETE /api/v1/users/{userId} → owner/admin check → call /internal cleanup on 3 services → DELETED (200) vs in-progress (202) -->

### Signup / authentication back-office

<!-- fill: signup creates account here; auth-service delegates credential checks to /internal/users/authenticate -->

### Follow graph queries

<!-- fill: isFollowing enrichment on search responses; following/followers exposed to post/notification services -->
