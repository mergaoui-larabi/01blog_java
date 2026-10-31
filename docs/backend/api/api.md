# API Endpoints by servecies

## api-gateway

**{host:port}/api/v1/\*\***

## auth-service

### GET

**{host:port}/.well-known/jwks.json**

**{host:port}/api/v1/auth/ping**

### POST

**{host:port}/api/v1/auth/signin**

### PUT

### PATCH

### DELETE

## user-service

### GET

### POST

### PUT

### PATCH

### DELETE

## media-service

### GET

### POST

### PUT

### PATCH

### DELETE

## moderation-service

### GET

#### 1. Fetch reports

`GET /api/v1/reports?status=OPEN&cursor=<lastReportId>&size=20`

- Administrator only: the caller's **current** role is re-checked against User Service on every request, so the `role` claim of an already-issued JWT is never trusted for moderation operations.
- `status`: optional filter (`OPEN`, `UNDER_REVIEW`, `RESOLVED`, `DISMISSED`).
- `cursor`: ID of the last report returned in the previous response, omitted for the first chunk.
- `size`: number of reports to fetch (1-100, default 20).
- Ordered by `(created_at, id)` descending.

Example response:

```json
{
  "content": [
    {
      "id": "report-uuid",
      "reporterId": "user-uuid",
      "targetType": "POST",
      "targetId": "post-uuid",
      "reason": "Spam and repeated harassment in the comments",
      "status": "OPEN",
      "createdAt": "2026-10-18T10:00:00Z",
      "updatedAt": "2026-10-18T10:00:00Z"
    }
  ],
  "nextCursor": "report-uuid",
  "hasNext": true
}
```

### POST

#### 1. Report a post

`POST /api/v1/reports/posts`

Requires any authenticated user. The reporter is taken from the authenticated identity; a `reporterId` in the body is ignored.

```json
{
  "postId": "post-uuid",
  "reason": "Spam and repeated harassment in the comments"
}
```

- Responds `201 Created` with the report body shown above, `status` always starting at `OPEN`.
- `reason` is required, non-blank and at most 2000 characters.

#### 2. Report a user

`POST /api/v1/reports/users`

Same contract as the post report, with `userId` instead of `postId` and `targetType` `USER`.

### PUT

### PATCH

#### 1. Update report status

`PATCH /api/v1/reports/{reportId}`

Administrator only (verified against User Service).

```json
{
  "status": "UNDER_REVIEW"
}
```

- Responds `200` with the updated report, `400` for an unknown status, `403` when the caller is not currently an administrator, `404` for an unknown report.
- Each accepted change writes a `STATUS_CHANGED` row in `moderation_actions` (`OPEN -> UNDER_REVIEW` in `details`) within the same transaction.

### DELETE

#### 1. Delete a report

`DELETE /api/v1/reports/{reportId}`

- Administrator only (verified against User Service), responds `204 No Content`.
- Responds `409 Conflict` when the report already carries moderation actions, since its audit history must not be discarded.

## notification-service

### GET

### POST

### PUT

### PATCH

### DELETE

## Post Service

### GET

#### 1. Fetch feed

`GET /api/v1/posts/feed?cursor=<lastPostId>&size=20`

- `cursor`: ID of the last post returned in the previous response.
- `size`: Number of posts to fetch.
- If `cursor` is omitted, fetch the first chunk.
- Use cursor-based pagination with a stable ordering such as `(created_at, id)` to avoid duplicates when new posts are inserted.
- The feed includes posts from users the authenticated user follows and their own posts.

#### 2. Fetch a specific post

`GET /api/v1/posts/{postId}`

- `postId`: ID of the post to retrieve.
- The response should include the post content, media references, author ID, timestamps, likes count, comments count, and whether the authenticated user has liked the post.

Example response:

```json
{
  "id": "post-uuid",
  "authorId": "user-uuid",
  "content": "Learning Spring Boot! **Transactions** are interesting.",
  "mediaId": ["media-uuid-1"],
  "likesCount": 12,
  "commentsCount": 4,
  "likedByCurrentUser": true,
  "createdAt": "2026-10-18T10:00:00Z",
  "updatedAt": "2026-10-18T10:00:00Z"
}
```

#### 3. Fetch comments

`GET /api/v1/posts/{postId}/comments?cursor=<lastCommentId>&size=20`

- `postId`: ID of the post.
- `cursor`: ID of the last comment returned in the previous response.
- `size`: Number of comments to fetch.
- If `cursor` is omitted, fetch the first chunk.

#### 4. Fetch the authenticated user's posts

`GET /api/v1/posts/me?cursor=<lastPostId>&size=20`

- Returns posts created by the authenticated user.
- `cursor`: ID of the last post returned in the previous response.
- `size`: Number of posts to fetch.

---

### POST

#### 1. Create a post

`POST /api/v1/posts`

```json
{
  "content": "My post content supports **Markdown**.",
  "mediaId": "media-uuid-1"
}
```

- `content`: Required, cannot be empty or blank. Supports Markdown.
- `mediaId`: Optional array of media IDs. Can be empty for text-only posts.
- The authenticated user's ID is extracted from the JWT.
- The Post Service validates that the media attachments are valid and authorized before creating the post.

#### 2. Like a post

`POST /api/v1/posts/{postId}/likes`

- `postId`: ID of the post to like.
- The authenticated user's ID is extracted from the JWT.
- A user can like a post only once.
- Repeating the request must not increment `likesCount` again.

#### 3. Add a comment

`POST /api/v1/posts/{postId}/comments`

```json
{
  "content": "My comment supports **Markdown**."
}
```

- `postId`: ID of the post to comment on.
- `content`: Required, cannot be empty or blank. Supports Markdown.
- The authenticated user's ID is extracted from the JWT.
- Increment `commentsCount` only when the comment is successfully created.

---

### PUT

#### 1. Replace a post's editable fields

`PUT /api/v1/posts/{postId}`

- Requires all editable fields.
- Replaces the post's content and media associations.
- Fields such as `authorId`, `likesCount`, `commentsCount`, and `createdAt` are managed by the server.

```json
{
  "content": "Updated post content supports **Markdown**.",
  "mediaId": "media-uuid-1"
}
```

- `content`: Required, cannot be empty or blank.
- `mediaId`: Required array. An empty array removes all media attachments.

#### 2. Replace a comment's content

`PUT /api/v1/posts/{postId}/comments/{commentId}`

```json
{
  "content": "Updated comment content supports **Markdown**."
}
```

- `content`: Required, cannot be empty or blank.
- The authenticated user must own the comment unless an authorized administrator is performing the operation.

---

### PATCH

#### 1. Partially update a post

`PATCH /api/v1/posts/{postId}`

- Requires at least one editable field.
- Fields omitted from the request remain unchanged.
- Explicitly setting `mediaId` to an empty array removes all media attachments.

Example: update content only.

```json
{
  "content": "Updated post content."
}
```

Example: update hidden state.

```json
{
  "hidden": true
}
```

Example: update media only.

```json
{
  "mediaId": ""
}
```

Example: remove all media.

```json
{
  "mediaId": ""
}
```

Example: update both fields.

```json
{
  "content": "Updated content.",
  "mediaId": "media-uuid-3"
}
```

#### 2. Partially update a comment

`PATCH /api/v1/posts/{postId}/comments/{commentId}`

```json
{
  "content": "Updated comment content."
}
```

- Only the supplied fields are updated.
- The authenticated user must own the comment unless an authorized administrator is performing the operation.

---

### DELETE

#### 1. Delete a post

`DELETE /api/v1/posts/{postId}`

- Deletes the post.
- Removes associated likes, comments, and post-media relationships.
- Media files are not automatically deleted from the Media Service because they might be referenced elsewhere.
- The authenticated user must own the post unless an authorized administrator is performing the operation.

#### 2. Unlike a post

`DELETE /api/v1/posts/{postId}/likes`

- Removes the authenticated user's like from the post.
- Decrement `likesCount` only if a like record was actually deleted.
- Repeating the request must not decrement the count again.

#### 3. Delete a comment

`DELETE /api/v1/posts/{postId}/comments/{commentId}`

- Deletes the specified comment.
- Decrement `commentsCount` only if a comment was actually deleted.
- The authenticated user must own the comment unless an authorized administrator is performing the operation.

---

## Additional Rules

- **Authentication:** All write operations require a valid JWT.
- **Authorization:** Users can modify or delete their own posts and comments. Administrators may perform authorized moderation actions.
- **Media:** Media metadata and files belong to the Media Service. The Post Service stores media IDs and validates attachments through the Media Service.
- **Likes:** Enforce a unique constraint on `(post_id, user_id)` in the database.
- **Counters:** Update `likesCount` and `commentsCount` atomically with their corresponding database operations.
- **Pagination:** Use cursor-based pagination with stable ordering, such as `(created_at, id)`.
- **Markdown:** Store Markdown source as text and sanitize rendered output in Angular.
- **Validation:** Reject blank post and comment content.
- **Timestamps:** Generate `createdAt` and `updatedAt` on the server.
- **Resource IDs:** Extract the current user's ID from the validated JWT rather than accepting an arbitrary `authorId` from the client.
