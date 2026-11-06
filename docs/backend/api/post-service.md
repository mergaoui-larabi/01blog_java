# post-service API

Part of the [API Reference](api.md). Posts, the authenticated feed, likes and comments.

Base: `http://localhost:5004` — routed via gateway at `/api/v1/posts/**`. Authenticated except `GET /{postId}` and `GET /{postId}/comments` (auth optional).

## Posts

### GET `/api/v1/posts/feed`

- **Used for**: the home feed — cursor-paginated posts from authors the caller follows.
- **Query params**: `cursor` (UUID), `size` (1–100, default 20).

### GET `/api/v1/posts/{postId}`

- **Used for**: fetching a single post (detail view). Works signed-in or anonymous; when signed in the response can include caller-specific state.

### GET `/api/v1/posts/me`

- **Used for**: the caller's own posts, cursor-paginated (`cursor`, `size`).

### POST `/api/v1/posts`

- **Used for**: publishing a new post.
- **Body**: `{ "content": string (required), "mediaIds": string[] }` (IDs returned by media-service).
- After creation the author's followers are notified via notification-service `POST /internal/notifications/post-created`.

### PUT `/api/v1/posts/{postId}` / PATCH `/api/v1/posts/{postId}`

- **Used for**: full replace vs partial update of the caller's post.
- **PUT body**: `{ "content": string (required), "mediaIds": string[] }`
- **PATCH body**: `{ "content"?: string, "mediaIds"?: string[], "hidden"?: boolean }`

### DELETE `/api/v1/posts/{postId}`

- **Used for**: deleting a post — author or admin. Returns `204`.

### POST `/api/v1/posts/{postId}/likes` / DELETE `/api/v1/posts/{postId}/likes`

- **Used for**: liking / unliking a post. `200` / `204`.

## Comments

- GET `/api/v1/posts/{postId}/comments?cursor=&size=` — **Used for**: listing a post's comments (cursor-paginated, public).
- POST `/api/v1/posts/{postId}/comments` — **Used for**: adding a comment. Body `{ "content": string (≤2000 chars) }`.
- PUT `/api/v1/posts/{postId}/comments/{commentId}` — **Used for**: full replacement of the caller's comment body.
- PATCH `/api/v1/posts/{postId}/comments/{commentId}` — **Used for**: partial update of the caller's comment body.
- DELETE `/api/v1/posts/{postId}/comments/{commentId}` — **Used for**: deleting a comment — author or admin. Returns `204`.

## Admin routes (`/api/v1/posts/admin`, JWT `role` claim must be ADMIN)

- GET `/api/v1/posts/admin/search?postId=&authorId=&cursor=&size=` — **Used for**: moderation search of posts by ID or author.
- PATCH `/api/v1/posts/admin/{postId}/hidden` — **Used for**: hiding/unhiding a post. Body `{ "hidden": boolean }`.
- DELETE `/api/v1/posts/admin/{postId}` — **Used for**: admin deletion of any post. Returns `204`.

## Internal

- POST `/internal/users/{userId}/cleanup` — **Used for**: user-service, during account deletion, to remove the user's posts. Not routed through the gateway.
