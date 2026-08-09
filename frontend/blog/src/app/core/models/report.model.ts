import { User } from './user.model';

export interface Report {
  id: number;
  reportedUser: User;
  reportedBy: User;
  reason: string;
  createdAt: string;
  status: 'PENDING' | 'RESOLVED' | 'DISMISSED';
}

export interface CreateReportRequest {
  reportedUserId: number;
  reason: string;
}
