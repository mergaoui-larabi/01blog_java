import { inject, Service } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environments';
import { ExploreApiService, PageResponse, UserSearch } from '../../explore/data-access/explore.api.service';

export interface UserMutationResult {
  id: string;
  banned: boolean;
}

@Service()
export class AdminApiService {
  private readonly http = inject(HttpClient);
  private readonly exploreApi = inject(ExploreApiService);

  private readonly env = environment;

  fetchUsers(
    lastUserId?: string | null,
    lastUserCreatedAt?: string | null,
    search?: string | null,
    size = 20,
  ): Observable<PageResponse<UserSearch>> {
    return this.exploreApi.fetchUsers(lastUserId, lastUserCreatedAt, search, size);
  }

  banUser(userId: string): Observable<UserMutationResult> {
    const url = this.env.banUserEndpoint.replace('{userId}', encodeURIComponent(userId));
    return this.http.patch<UserMutationResult>(url, {});
  }

  unbanUser(userId: string): Observable<UserMutationResult> {
    const url = this.env.unbanUserEndpoint.replace('{userId}', encodeURIComponent(userId));
    return this.http.patch<UserMutationResult>(url, {});
  }

  deleteUser(userId: string): Observable<void> {
    const url = this.env.deleteUserEndpoint.replace('{userId}', encodeURIComponent(userId));
    return this.http.delete<void>(url);
  }
}
