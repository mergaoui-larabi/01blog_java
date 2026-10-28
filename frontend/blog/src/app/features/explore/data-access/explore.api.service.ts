import { inject, Service } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environments';

export interface UserSearch {
  id: string;
  email: string;
  username: string;
  age: number;
  gender: string;
  role: string;
  avatarMediaId: string | null;
  banned: boolean;
  followedByCurrentUser: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface PageResponse<T> {
  content: T[];
  nextCursor: string | null;
}

@Service()
export class ExploreApiService {
  private readonly http = inject(HttpClient);
  private readonly env = environment;

  //get

  fetchUsers(
    lastUserId?: string | null,
    lastUserCreatedAt?: string | null,
    search?: string | null,
    size = 20,
  ): Observable<PageResponse<UserSearch>> {
    let params = new HttpParams().set('size', size);
    if (lastUserId) {
      params = params.set('cursor', lastUserId);
    }
    if (lastUserCreatedAt) {
      params = params.set('cursorInstant', lastUserCreatedAt);
    }
    if (search) {
      params = params.set('search', search);
    }

    params = params.set('size', size);

    const url = this.env.getUsersEndPoint.split('?')[0];

    return this.http.get<PageResponse<UserSearch>>(url, { params });
  }

  //post
  followUser(userId: string): Observable<void> {
    const url = this.env.postFollowUser.replace('{userId}', encodeURIComponent(userId));
    return this.http.post<void>(url, {});
  }

  //delete
  unfollowUser(userId: string): Observable<void> {
    const url = this.env.deleteFollowUser.replace('{userId}', encodeURIComponent(userId));
    return this.http.delete<void>(url);
  }
}
