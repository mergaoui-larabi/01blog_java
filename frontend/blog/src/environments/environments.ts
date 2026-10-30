export const environment = {
  apiUrl: 'http://localhost:5000/api/v1',

  // auth
  postLoginEndPoint: 'http://localhost:5000/api/v1/auth/signin',
  authPingEndpoint: 'http://localhost:5000/api/v1/auth/ping',

  // user
  postRegisterEndPoint: 'http://localhost:5000/api/v1/users/signup',
  getMeEndpoint: 'http://localhost:5000/api/v1/users/me',
  patchAvatarIdEndpoint: 'http://localhost:5000/api/v1/users/update',
  updateUserAvatarEndpoint: 'http://localhost:5000/api/v1/users/update',
  getUsersEndPoint:
    'http://localhost:5000/api/v1/users?cursor={lastUserId}&cursorInstant={cursorInstant}&search={search}&size=20',
  getUserByIdEndpoint: 'http://localhost:5000/api/v1/users/id/{userId}',
  getUserByUsernameEndpoint: 'http://localhost:5000/api/v1/users/{username}',
  postFollowUser: 'http://localhost:5000/api/v1/users/{userId}/follow',
  deleteFollowUser: 'http://localhost:5000/api/v1/users/{userId}/follow',
  followUserEndpoint: 'http://localhost:5000/api/v1/users/{userId}/follow',
  unfollowUserEndpoint: 'http://localhost:5000/api/v1/users/{userId}/follow',
  banUserEndpoint: 'http://localhost:5000/api/v1/users/{userId}/ban',
  unbanUserEndpoint: 'http://localhost:5000/api/v1/users/{userId}/unban',
  deleteUserEndpoint: 'http://localhost:5000/api/v1/users/{userId}',
  userPingEndpoint: 'http://localhost:5000/api/v1/users/ping',

  // media
  postAvatarEndpoint: 'http://localhost:5000/api/v1/medias/avatars',
  uploadAvatarEndpoint: 'http://localhost:5000/api/v1/medias/avatars',
  uploadpostsMediaEndpoint: 'http://localhost:5000/api/v1/medias/posts',
  uploadPostMediaEndpoint: 'http://localhost:5000/api/v1/medias/posts',
  getAvatarMediaEndpoint: 'http://localhost:5000/api/v1/medias/{mediaId}/avatar',
  getMediaEndpoint: 'http://localhost:5000/api/v1/medias/{mediaId}/content',
  getMediaContentEndpoint: 'http://localhost:5000/api/v1/medias/{mediaId}/content',
  updateMediaEndpoint: 'http://localhost:5000/api/v1/medias/{mediaId}',
  deleteMediaEndpoint: 'http://localhost:5000/api/v1/medias/{mediaId}',
  mediaPingEndpoint: 'http://localhost:5000/api/v1/medias/ping',

  // post
  fetchFeedEndPoint: 'http://localhost:5000/api/v1/posts/feed?cursor={lastPostId}&size=20',
  fetchPostEndPoint: 'http://localhost:5000/api/v1/posts/{postId}',
  fetchCommentsEndPoint:
    'http://localhost:5000/api/v1/posts/{postId}/comments?cursor={lastCommentId}&size=20',
  fetchMyPostsEndPoint: 'http://localhost:5000/api/v1/posts/me?cursor={lastPostId}&size=20',

  createPostEndPoint: 'http://localhost:5000/api/v1/posts',
  likePostEndPoint: 'http://localhost:5000/api/v1/posts/{postId}/likes',
  createCommentEndPoint: 'http://localhost:5000/api/v1/posts/{postId}/comments',

  replacePostEndPoint: 'http://localhost:5000/api/v1/posts/{postId}',
  replaceCommentEndPoint: 'http://localhost:5000/api/v1/posts/{postId}/comments/{commentId}',

  updatePostEndPoint: 'http://localhost:5000/api/v1/posts/{postId}',
  updateCommentEndPoint: 'http://localhost:5000/api/v1/posts/{postId}/comments/{commentId}',

  deletePostEndPoint: 'http://localhost:5000/api/v1/posts/{postId}',
  unlikePostEndPoint: 'http://localhost:5000/api/v1/posts/{postId}/likes',
  deleteCommentEndPoint: 'http://localhost:5000/api/v1/posts/{postId}/comments/{commentId}',

  // report
  createReportEndpoint: 'http://localhost:5000/api/v1/reports',
  getMyReportsEndpoint: 'http://localhost:5000/api/v1/reports/mine',
  getReportEndpoint: 'http://localhost:5000/api/v1/reports/{reportId}',
  updateReportEndpoint: 'http://localhost:5000/api/v1/reports/{reportId}',
  deleteReportEndpoint: 'http://localhost:5000/api/v1/reports/{reportId}',

  // notification
} as const;
