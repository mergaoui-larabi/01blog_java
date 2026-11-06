# media-service

Stores uploaded files (avatars, post images/videos) and their metadata; serves the bytes back.

## 1. Directory tree (packages)

```
src/main/java/com/blog/media/
├── application/
│   └── dto/
│       ├── error/
│       ├── request/
│       └── response/
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
calls ──► media-service :5003
         └──► (no other backend service; only file storage + DB)

called by
├── post-service  media validation when creating/updating posts
└── clients       /api/v1/medias/** (via gateway): uploads, downloads, updates, deletes
```

## 3. Packages

### application/dto

<!-- fill: UpdateMediaRequest, MediaResponse, error DTOs -->

### config

<!-- fill: storage location/config properties -->

### constant

<!-- fill -->

### controller

<!-- fill: MediaController (avatars, posts, avatar/content bytes, PUT, DELETE, ping) -->

### domain

<!-- fill: Media entity, purpose/status models, value objects -->

### exception

<!-- fill -->

### filter

<!-- fill -->

### repository

<!-- fill -->

### service

<!-- fill: MediaService — createAvatar, createPostMedia, getContent, updateMedia, deleteMedia, content-type detection -->

## 4. Important flows

### Upload → attach → display

<!-- fill: client uploads to /medias/posts (201 gives media IDs) → IDs sent in POST /api/v1/posts → bytes fetched via /{mediaId}/content -->

### Ownership enforcement

<!-- fill: JWT sub as owner; content reads owner-only, update/delete owner-or-admin -->
