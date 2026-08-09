import { User } from './user.model';

export interface Post {
  id: number;
  title: string;
  content: string;
  mediaUrl?: string;
  mediaType?: 'image' | 'video';
  author: User;
  createdAt: string;
  updatedAt: string;
  likeCount: number;
  commentCount: number;
  isLiked?: boolean;
}

export interface CreatePostRequest {
  title: string;
  content: string;
  media?: File;
}

export interface UpdatePostRequest {
  title?: string;
  content?: string;
  media?: File;
}
