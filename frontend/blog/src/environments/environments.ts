export const environment = {
  apiUrl: 'http://localhost:5000/api/v1',
  postLoginEndPoint: 'http://localhost:5000/api/v1/auth/signin',
  postRegisterEndPoint: 'http://localhost:5000/api/v1/users/signup',
  getMeEndpoint: 'http://localhost:5000/api/v1/users/me',
  postAvatarEndpoint: 'http://localhost:5000/api/v1/medias/avatars',
  patchAvatarIdEndpoint: 'http://localhost:5000/api/v1/users/update',
} as const;
