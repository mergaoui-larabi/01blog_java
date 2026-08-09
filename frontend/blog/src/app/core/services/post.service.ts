import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Post, CreatePostRequest, UpdatePostRequest } from '../models/post.model';

@Injectable({ providedIn: 'root' })
export class PostService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/posts';

  getFeed(page: number = 0, size: number = 10): Observable<Post[]> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Post[]>(`${this.apiUrl}/feed`, { params });
  }

  getPost(id: number): Observable<Post> {
    return this.http.get<Post>(`${this.apiUrl}/${id}`);
  }

  getUserPosts(username: string, page: number = 0, size: number = 10): Observable<Post[]> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Post[]>(`/api/users/${username}/posts`, { params });
  }

  createPost(data: CreatePostRequest): Observable<Post> {
    const formData = new FormData();
    formData.append('title', data.title);
    formData.append('content', data.content);
    if (data.media) {
      formData.append('media', data.media);
    }
    return this.http.post<Post>(this.apiUrl, formData);
  }

  updatePost(id: number, data: UpdatePostRequest): Observable<Post> {
    const formData = new FormData();
    if (data.title) formData.append('title', data.title);
    if (data.content) formData.append('content', data.content);
    if (data.media) formData.append('media', data.media);
    return this.http.put<Post>(`${this.apiUrl}/${id}`, formData);
  }

  deletePost(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  likePost(id: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${id}/like`, {});
  }

  unlikePost(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}/like`);
  }
}
