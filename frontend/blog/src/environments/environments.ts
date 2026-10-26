export const environment = {
  apiUrl: 'http://localhost:5000/api/v1',

  // auth
  postLoginEndPoint: 'http://localhost:5000/api/v1/auth/signin',

  // user
  postRegisterEndPoint: 'http://localhost:5000/api/v1/users/signup',
  getMeEndpoint: 'http://localhost:5000/api/v1/users/me',
  patchAvatarIdEndpoint: 'http://localhost:5000/api/v1/users/update',

  // media
  postAvatarEndpoint: 'http://localhost:5000/api/v1/medias/avatars',
  uploadpostsMediaEndpoint: 'http://localhost:5000/api/v1/medias/posts',
  getAvatarMediaEndpoint: 'http://localhost:5000/api/v1/medias/{mediaId}/avatar',
  getMediaEndpoint: 'http://localhost:5000/api/v1/medias/{mediaId}/content',

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

  // notification

  // feed
} as const;
