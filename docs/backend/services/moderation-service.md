# moderation-service

User reports (posts/users) and the admin workflow for triaging them.

## 1. Directory tree (packages)

```
src/main/java/com/blog/moderation/
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
├── exception/
├── filter/
├── repository/
└── service/
```

## 2. Dependency tree

```
calls ──► moderation-service :5006
         └──► user-service :5001  GET /internal/users/{id}/role (live admin re-check)

called by
├── user-service  POST /internal/users/{id}/cleanup (account deletion)
└── clients       /api/v1/reports/** (via gateway)
```

## 3. Packages

### application/dto

<!-- fill: CreatePostReportRequest, CreateUserReportRequest, UpdateReportRequest, ReportResponse, PageResponse, UserCleanupResponse -->

### client

<!-- fill: user-service role client + client/dto -->

### config

<!-- fill: RestClientConfig, SecurityConfig -->

### constant

<!-- fill: SecurityConstants -->

### controller

<!-- fill: ReportController, InternalUserCleanupController -->

### domain

<!-- fill: Report entity, ReportStatus/ReportTarget models -->

### exception

<!-- fill -->

### filter

<!-- fill -->

### repository

<!-- fill: cursor-paginated report queries by status -->

### service

<!-- fill: ReportService, AdminAuthorizationService (re-check vs JWT claim), ReportCleanupService -->

## 4. Important flows

### Live admin authorization

<!-- fill: every moderator action calls AdminAuthorizationService.requireAdmin → GET /internal/users/{id}/role on user-service, so a demoted admin's stale JWT role is never trusted -->

### Report lifecycle

<!-- fill: OPEN → UNDER_REVIEW → RESOLVED/DISMISSED via PATCH /reports/{id}; who acted is recorded; DELETE removes -->
