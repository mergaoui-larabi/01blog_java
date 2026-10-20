export const environment = {
  apiUrl: 'http://localhost:5000/api/v1',
  //auth
  postLoginEndPoint: 'http://localhost:5000/api/v1/auth/signin',
  //user
  postRegisterEndPoint: 'http://localhost:5000/api/v1/users/signup',
  getMeEndpoint: 'http://localhost:5000/api/v1/users/me',
  patchAvatarIdEndpoint: 'http://localhost:5000/api/v1/users/update',
  //media
  postAvatarEndpoint: 'http://localhost:5000/api/v1/medias/avatars',
  uploadpostsMediaEndpoint: 'http://localhost:5000/api/v1/medias/posts',
  //post
  fetchFeedEndPoint: '/api/v1/posts/feed?cursor={lastPostId}&size=20',
  fetchPostEndPoint: '/api/v1/posts/{postId}',
  fetchCommentsEndPoint: '/api/v1/posts/{postId}/comments?cursor={lastCommentId}&size=20',
  fetchMyPostsEndPoint: '/api/v1/posts/me?cursor={lastPostId}&size=20',

  createPostEndPoint: '/api/v1/posts',
  likePostEndPoint: '/api/v1/posts/{postId}/likes',
  createCommentEndPoint: '/api/v1/posts/{postId}/comments',

  replacePostEndPoint: '/api/v1/posts/{postId}',
  replaceCommentEndPoint: '/api/v1/posts/{postId}/comments/{commentId}',

  updatePostEndPoint: '/api/v1/posts/{postId}',
  updateCommentEndPoint: '/api/v1/posts/{postId}/comments/{commentId}',

  deletePostEndPoint: '/api/v1/posts/{postId}',
  unlikePostEndPoint: '/api/v1/posts/{postId}/likes',
  deleteCommentEndPoint: '/api/v1/posts/{postId}/comments/{commentId}',
  //report
  //notifcation
  //feed
} as const;
