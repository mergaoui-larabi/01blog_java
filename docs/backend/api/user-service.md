# user-service API

Part of the [API Reference](api.md). Owns user accounts, profiles, follow graph, bans and account deletion.

Base: `http://localhost:5001` — routed via gateway at `/api/v1/users/**`. Authenticated except `signup` and `ping`.

## GET `/api/v1/users/me`

- **Used for**: the logged-in user fetching their own full profile (email, username, age, gender, role, avatar, banned flag, timestamps).

## GET `/api/v1/users`

- **Used for**: browsing/searching the user directory (e.g. search box, "people" lists). Returns a page of `UserSearchResponse` including whether the **caller** follows each user.
- **Query params**: `cursor` (UUID), `cursorInstant` (ISO timestamp), `search` (username text), `size` (1–100, default 20).

## GET `/api/v1/users/id/{userId}`

- **Used for**: fetching one user's profile by ID (with `isFollowing` for the caller) — e.g. opening a user's page from an avatar on a post.

## GET `/api/v1/users/{username}`

- **Used for**: fetching one user's profile by username (with `isFollowing` and `isSelf` flags) — profile pages addressed by `@username`.

## POST `/api/v1/users/signup`

- **Used for**: creating a new account. Public (no JWT).
- **Body**: `{ "email": string, "username": string, "password": string, "age": number, "gender": string }`
- **Returns**: `201` with the created `UserResponse`.

## PATCH `/api/v1/users/update`

- **Used for**: setting the profile's avatar by pointing it at an already-uploaded media ID (the media itself is uploaded via media-service).
- **Body**: `{ "ownerId": UUID, "avatarMediaId": UUID }`
- **Returns**: `201` with the updated `UserResponse`.

## POST `/api/v1/users/{userId}/follow`

- **Used for**: following a user. Rejected if the caller's account is not active. Returns `200`.

## DELETE `/api/v1/users/{userId}/follow`

- **Used for**: unfollowing a user. Returns `204`.

## PATCH `/api/v1/users/{userId}/ban` / PATCH `/api/v1/users/{userId}/unban`

- **Used for**: admins banning or unbanning an account. Banned users are refused by login and by write operations. Returns `200` with the updated user.

## DELETE `/api/v1/users/{userId}`

- **Used for**: deleting an account (owner or admin only). User-service orchestrates the full deletion, calling `/internal/users/{userId}/cleanup` on post, notification and moderation services to wipe the user's data everywhere.
- **Returns**: `200` when fully deleted, `202` when deletion is still in progress (`AccountDeletionResponse`).

## GET `/api/v1/users/ping`

- **Used for**: health probe — returns `pong`.

## Internal (service-to-service, not routed through the gateway)

- POST `/internal/users/authenticate` — **Used for**: auth-service verifying credentials at signin. Body `{ email, password }`, returns token/role info.
- GET `/internal/users/{userId}/following` — **Used for**: post-service building a feed from the IDs this user follows.
- GET `/internal/users/{userId}/followers` — **Used for**: notification-service finding a post author's active followers to notify.
- GET `/internal/users/{userId}/role` — **Used for**: moderation-service re-checking the caller's *current* role live instead of trusting the JWT `role` claim.
