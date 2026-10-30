import { inject, Service } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environments';
import { UserSearch } from '../../explore/data-access/explore.api.service';

export interface Post {
  id: string;
  authorId: string;
  content: string;
  mediaIds: string[];
  likesCount: number;
  commentsCount: number;
  likedByCurrentUser: boolean;
  ownedByCurrentUser: boolean;
  hidden: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Comment {
  id: string;
  postId: string;
  authorId: string;
  content: string;
  createdAt: string;
  updatedAt: string;
}

export interface PageResponse<T> {
  content: T[];
  nextCursor: string | null;
  hasNext: boolean;
}

export interface CreatePostRequest {
  content: string;
  mediaIds: string[];
}

export interface ReplacePostRequest {
  content: string;
  mediaIds: string[];
}

export interface UpdatePostRequest {
  content?: string;
  mediaIds?: string[];
  hidden?: boolean;
}

export interface CreateCommentRequest {
  content: string;
}

export interface UpdateCommentRequest {
  content: string;
}

export interface MediaResponse {
  id: string;
  ownerId: string;
  purpose: string;
  status: string;
  originalFilename: string;
  detectedContentType: string;
  sizeBytes: number;
  width: number | null;
  height: number | null;
  durationMs: number | null;
  createdAt: string;
  updatedAt: string;
}

export interface MediaItem {
  id: string;
  file: File;
  previewUrl: string;
  type: 'image' | 'video';
}

@Service()
export class BlockApiService {
  private readonly http = inject(HttpClient);
  private readonly env = environment;

  //get
  fetchFeed(lastPostId?: string | null, size: number = 20): Observable<PageResponse<Post>> {
    let params = new HttpParams().set('size', size);
    if (lastPostId) {
      params = params.set('cursor', lastPostId);
    }
    return this.http.get<PageResponse<Post>>(this.env.fetchFeedEndPoint.split('?')[0], {
      params,
    });
  }

  fetchPost(postId: string): Observable<Post> {
    return this.http.get<Post>(
      this.env.fetchPostEndPoint.replace('{postId}', encodeURIComponent(postId)),
    );
  }

  fetchComments(
    postId: string,
    lastCommentId?: string | null,
    size: number = 20,
  ): Observable<PageResponse<Comment>> {
    let params = new HttpParams().set('size', size);
    if (lastCommentId) {
      params = params.set('cursor', lastCommentId);
    }
    const url = this.env.fetchCommentsEndPoint
      .split('?')[0]
      .replace('{postId}', encodeURIComponent(postId));

    return this.http.get<PageResponse<Comment>>(url, { params });
  }

  fetchMyPosts(lastPostId?: string | null, size = 20): Observable<PageResponse<Post>> {
    let params = new HttpParams().set('size', size);
    if (lastPostId) {
      params = params.set('cursor', lastPostId);
    }
    const url = this.env.fetchMyPostsEndPoint.split('?')[0];

    return this.http.get<PageResponse<Post>>(url, { params });
  }

  getUser(username: string): Observable<UserSearch> {
    const url = this.env.getUserByUsernameEndpoint.replace(
      '{username}',
      encodeURIComponent(username),
    );
    return this.http.get<UserSearch>(url);
  }
}
