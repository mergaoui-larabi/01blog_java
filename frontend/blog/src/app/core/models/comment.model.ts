import { User } from './user.model';

export interface Comment {
  id: number;
  content: string;
  author: User;
  postId: number;
  createdAt: string;
}

export interface CreateCommentRequest {
  content: string;
}
