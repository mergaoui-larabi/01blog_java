import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../models/user.model';
import { Post } from '../models/post.model';
import { Report } from '../models/report.model';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/admin';

  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/users`);
  }

  getPosts(): Observable<Post[]> {
    return this.http.get<Post[]>(`${this.apiUrl}/posts`);
  }

  getReports(): Observable<Report[]> {
    return this.http.get<Report[]>(`${this.apiUrl}/reports`);
  }

  deleteUser(userId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/users/${userId}`);
  }

  banUser(userId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/users/${userId}/ban`, {});
  }

  unbanUser(userId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/users/${userId}/unban`, {});
  }

  deletePost(postId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/posts/${postId}`);
  }

  resolveReport(reportId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/reports/${reportId}/resolve`, {});
  }

  dismissReport(reportId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/reports/${reportId}/dismiss`, {});
  }
}
