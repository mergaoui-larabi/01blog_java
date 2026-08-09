import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/users';

  getUser(username: string): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${username}`);
  }

  updateProfile(username: string, data: Partial<User>): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${username}`, data);
  }

  subscribe(username: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${username}/subscribe`, {});
  }

  unsubscribe(username: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${username}/subscribe`);
  }

  searchUsers(query: string): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/search`, { params: { q: query } });
  }
}
