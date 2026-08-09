export interface User {
  id: number;
  username: string;
  email: string;
  bio: string;
  avatarUrl: string;
  role: 'USER' | 'ADMIN';
  createdAt: string;
  subscriberCount: number;
  postCount: number;
  isSubscribed?: boolean;
  isBanned?: boolean;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}
