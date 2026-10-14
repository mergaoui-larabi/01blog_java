# Media Service

Stores avatars and post images or videos (`:5003`, PostgreSQL + Flyway, local disk storage). Every stored file belongs to exactly one user: reads are owner-only, writes are owner or admin.

## Upload

```mermaid
%%{init: {"theme":"base","themeVariables":{"background":"#ffffff","git0":"#0f172a","gitBranchLabel0":"#f8fafc","cScale0":"#0f172a","cScaleLabel0":"#f8fafc","cScale1":"#334155","cScaleLabel1":"#f8fafc","cScale2":"#334155","cScaleLabel2":"#f8fafc","cScale3":"#334155","cScaleLabel3":"#f8fafc","cScale4":"#334155","cScaleLabel4":"#f8fafc","cScale5":"#334155","cScaleLabel5":"#f8fafc","cScale6":"#334155","cScaleLabel6":"#f8fafc"}}}%%
mindmap
  root((Upload))
    "1. Pick an endpoint"
      "POST /api/v1/medias/avatars"
      "POST /api/v1/medias/posts"
      "Owner from JWT sub claim"
    "2. Validate"
      "Empty or over 100MB returns 400"
      "Allowed: jpeg png gif webp avif mp4 webm ogg"
      "Purpose must match content type"
    "3. Store"
      "Stream to temp file, then move"
      "Save the row as ACCEPTED"
      "Row fails, delete the file"
```

## Read, update, delete

```mermaid
%%{init: {"theme":"base","themeVariables":{"background":"#ffffff","git0":"#0f172a","gitBranchLabel0":"#f8fafc","cScale0":"#0f172a","cScaleLabel0":"#f8fafc","cScale1":"#334155","cScaleLabel1":"#f8fafc","cScale2":"#334155","cScaleLabel2":"#f8fafc","cScale3":"#334155","cScaleLabel3":"#f8fafc","cScale4":"#334155","cScaleLabel4":"#f8fafc","cScale5":"#334155","cScaleLabel5":"#f8fafc","cScale6":"#334155","cScaleLabel6":"#f8fafc"}}}%%
mindmap
  root((Consume))
    "Read, owner only"
      "GET /api/v1/medias/mediaId returns metadata"
      "GET /api/v1/medias/mediaId/content returns bytes"
      "Not the owner returns 403"
      "Unknown id returns 404"
    "Update, owner or admin"
      "PUT /api/v1/medias/mediaId"
      "Replace file or purpose"
      "Nothing to change returns 400"
      "Old file deleted after save"
    "Delete, owner or admin"
      "DELETE /api/v1/medias/mediaId"
      "Row and bytes removed, responds 204"
```

## Storage and security

```mermaid
%%{init: {"theme":"base","themeVariables":{"background":"#ffffff","git0":"#0f172a","gitBranchLabel0":"#f8fafc","cScale0":"#0f172a","cScaleLabel0":"#f8fafc","cScale1":"#334155","cScaleLabel1":"#f8fafc","cScale2":"#334155","cScaleLabel2":"#f8fafc","cScale3":"#334155","cScaleLabel3":"#f8fafc","cScale4":"#334155","cScaleLabel4":"#f8fafc","cScale5":"#334155","cScaleLabel5":"#f8fafc","cScale6":"#334155","cScaleLabel6":"#f8fafc"}}}%%
mindmap
  root((Storage))
    "PostgreSQL + Flyway"
      "medias table, storage_key unique"
    "OAuth2 resource server"
      "Only /actuator/health is public"
    "No gateway route yet"
      "Reach it directly on :5003"
```
