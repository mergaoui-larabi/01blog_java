import { inject, Service } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environments';
import { ExploreApiService, PageResponse, UserSearch } from '../../explore/data-access/explore.api.service';
import { Post, PageResponse as PostPageResponse } from '../../posts/data-access/post.api.service';

export interface UserMutationResult {
  id: string;
  banned: boolean;
}

export type PostSearchField = 'all' | 'postId' | 'authorId';

export interface PostFilter {
  field: PostSearchField;
  value: string;
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

  fetchPosts(
    filter: PostFilter,
    cursor?: string | null,
    size = 20,
  ): Observable<PostPageResponse<Post>> {
    let params = new HttpParams().set('size', size);
    if (cursor) {
      params = params.set('cursor', cursor);
    }
    if (filter.value && filter.field !== 'all') {
      params = params.set(filter.field, filter.value);
    }

    return this.http.get<PostPageResponse<Post>>(this.env.adminSearchPostsEndpoint.split('?')[0], {
      params,
    });
  }

  hidePost(postId: string): Observable<Post> {
    return this.setPostHidden(postId, true);
  }

  unhidePost(postId: string): Observable<Post> {
    return this.setPostHidden(postId, false);
  }

  deletePost(postId: string): Observable<void> {
    const url = this.env.adminDeletePostEndpoint.replace('{postId}', encodeURIComponent(postId));
    return this.http.delete<void>(url);
  }

  private setPostHidden(postId: string, hidden: boolean): Observable<Post> {
    const url = this.env.adminHidePostEndpoint.replace('{postId}', encodeURIComponent(postId));
    return this.http.patch<Post>(url, { hidden });
  }
}
