# media-service API

Part of the [API Reference](api.md). Stores uploaded files (avatars, post images/videos) with metadata; serves their bytes.

Base: `http://localhost:5003` — routed via gateway at `/api/v1/medias/**`. Authenticated except `ping`.

## POST `/api/v1/medias/avatars`

- **Used for**: uploading the caller's avatar image. `multipart/form-data` with a single `file` part. Returns `201` with `MediaResponse` (ID goes into `PATCH /api/v1/users/update`).

## POST `/api/v1/medias/posts`

- **Used for**: uploading images and/or videos for a post. `multipart/form-data` with one or more `files` parts (purpose derived from content type). Returns `201` with a list of `MediaResponse`s; their IDs are sent in the post-create request.

## GET `/api/v1/medias/{mediaId}/avatar`

- **Used for**: streaming the avatar bytes (inline `Content-Disposition`), owner only. The frontend uses this to render profile pictures.

## GET `/api/v1/medias/{mediaId}/content`

- **Used for**: streaming any media file's bytes (image or video), owner only. Used to display a post's attached media.

## PUT `/api/v1/medias/{mediaId}`

- **Used for**: replacing the stored file and/or changing its `purpose`. Both `file` and `purpose` are optional `multipart/form-data` params. Owner or admin. Returns `200` with updated metadata.

## DELETE `/api/v1/medias/{mediaId}`

- **Used for**: deleting the metadata row and the stored bytes. Owner or admin. Returns `204`.

## GET `/api/v1/medias/ping`

- **Used for**: health probe — returns `pong`.
