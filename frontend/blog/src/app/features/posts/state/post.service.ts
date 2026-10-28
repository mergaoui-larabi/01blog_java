import { HttpErrorResponse } from '@angular/common/http';
import { inject, Service, signal } from '@angular/core';
import { EMPTY, from, Observable } from 'rxjs';
import {
  catchError,
  concatMap,
  finalize,
  map,
  single,
  switchMap,
  tap,
  toArray,
} from 'rxjs/operators';

import { CreatePostRequest, Post, PostApiService } from '../data-access/post.api.service';
import { AlertService } from '../../../core/alerts/alert.service';
import { Router } from '@angular/router';

@Service()
export class PostService {
  private readonly alertService = inject(AlertService);
  private readonly postApi = inject(PostApiService);
  private readonly router = inject(Router);

  readonly lastPost = signal<Post | null>(null);
  readonly loading = signal(false);

  createPost(content: string, mediaFiles: File[] | null): Observable<Post> {
    const files = mediaFiles ?? [];

    return from(files).pipe(
      concatMap((file) =>
        this.postApi.uploadMedia([file]).pipe(
          map((responses) => {
            const uploadedMedia = responses[0];

            if (!uploadedMedia?.id) {
              throw new Error(`The media service did not return an ID for "${file.name}".`);
            }

            return uploadedMedia.id;
          }),
        ),
      ),
      toArray(),
      switchMap((mediaIds) => this.postApi.createPost({ content, mediaIds })),
      tap(() => {
        this.alertService.success('Post published!');
      }),
      catchError((error: unknown) => {
        this.getErrorMessage(error);
        return EMPTY;
      }),
    );
  }

  private getErrorMessage(error: unknown): void {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 0) {
        this.alertService.error('Cannot connect to the server.');
        return;
      }
      const message = error.error?.message;
      return this.alertService.error(message);
    }
    this.alertService.error('An unexpected error occurred. Please try again.');
  }
}
