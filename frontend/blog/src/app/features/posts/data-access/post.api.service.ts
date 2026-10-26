import { inject, Service } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environments';

export interface Post {
  id: string;
  authorId: string;
  content: string;
  mediaIds: string[];
  likesCount: number;
  commentsCount: number;
  likedByCurrentUser: boolean;
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
  mediaId: string | null;
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
export class PostApiService {
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

  //post
  uploadMedia(files: File[]): Observable<MediaResponse[]> {
    const formData = new FormData();
    for (const file of files) {
      formData.append('files', file);
    }
    return this.http.post<MediaResponse[]>(this.env.uploadpostsMediaEndpoint, formData);
  }

  createPost(request: CreatePostRequest): Observable<Post> {
    return this.http.post<Post>(this.env.createPostEndPoint, request);
  }

  likePost(postId: string): Observable<void> {
    const url = this.env.likePostEndPoint.replace('{postId}', encodeURIComponent(postId));

    return this.http.post<void>(url, {});
  }

  createComment(postId: string, request: CreateCommentRequest): Observable<Comment> {
    const url = this.env.createCommentEndPoint.replace('{postId}', encodeURIComponent(postId));

    return this.http.post<Comment>(url, request);
  }

  replacePost(postId: string, request: ReplacePostRequest): Observable<Post> {
    const url = this.env.replacePostEndPoint.replace('{postId}', encodeURIComponent(postId));

    return this.http.put<Post>(url, request);
  }

  replaceComment(
    postId: string,
    commentId: string,
    request: UpdateCommentRequest,
  ): Observable<Comment> {
    const url = this.env.replaceCommentEndPoint
      .replace('{postId}', encodeURIComponent(postId))
      .replace('{commentId}', encodeURIComponent(commentId));

    return this.http.put<Comment>(url, request);
  }

  //patch
  updatePost(postId: string, request: UpdatePostRequest): Observable<Post> {
    const url = this.env.updatePostEndPoint.replace('{postId}', encodeURIComponent(postId));
    return this.http.patch<Post>(url, request);
  }

  updateComment(
    postId: string,
    commentId: string,
    request: UpdateCommentRequest,
  ): Observable<Comment> {
    const url = this.env.updateCommentEndPoint
      .replace('{postId}', encodeURIComponent(postId))
      .replace('{commentId}', encodeURIComponent(commentId));
    return this.http.patch<Comment>(url, request);
  }

  //delete
  deletePost(postId: string): Observable<void> {
    const url = this.env.deletePostEndPoint.replace('{postId}', encodeURIComponent(postId));

    return this.http.delete<void>(url);
  }

  unlikePost(postId: string): Observable<void> {
    const url = this.env.unlikePostEndPoint.replace('{postId}', encodeURIComponent(postId));

    return this.http.delete<void>(url);
  }

  deleteComment(postId: string, commentId: string): Observable<void> {
    const url = this.env.deleteCommentEndPoint
      .replace('{postId}', encodeURIComponent(postId))
      .replace('{commentId}', encodeURIComponent(commentId));
    return this.http.delete<void>(url);
  }
}
