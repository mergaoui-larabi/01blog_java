export interface Notification {
  id: number;
  type: 'NEW_POST' | 'SUBSCRIBE' | 'LIKE' | 'COMMENT';
  message: string;
  isRead: boolean;
  createdAt: string;
  relatedUserId?: number;
  relatedPostId?: number;
  relatedUsername?: string;
}
