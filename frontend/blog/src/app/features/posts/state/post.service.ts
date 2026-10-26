import { HttpErrorResponse } from '@angular/common/http';
import { inject, Service, signal } from '@angular/core';
import { EMPTY, from } from 'rxjs';
import {
  catchError,
  concatMap,
  finalize,
  map,
  switchMap,
  tap,
  toArray,
} from 'rxjs/operators';

import {
  CreatePostRequest,
  PostApiService,
} from '../data-access/post.api.service';

@Service()
export class PostService {
  private readonly postApi = inject(PostApiService);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  createPost(content: string, mediaFiles: File[] | null): void {
    if (this.loading()) return;

    this.loading.set(true);
    this.error.set(null);

    const files = mediaFiles ?? [];

    from(files)
      .pipe(
        // Upload one file at a time, preserving the input order.
        concatMap((file) =>
          this.postApi.uploadMedia([file]).pipe(
            map((responses) => {
              const uploadedMedia = responses[0];

              if (!uploadedMedia?.id) {
                throw new Error(
                  `The media service did not return an ID for "${file.name}".`,
                );
              }

              return uploadedMedia.id;
            }),
          ),
        ),

        // Collect IDs in upload order.
        toArray(),

        // Create the post only after all uploads succeed.
        switchMap((mediaIds) => {
          const request: CreatePostRequest = {
            content,
            mediaIds,
          };

          return this.postApi.createPost(request);
        }),

        tap((post) => {
          // Handle the successfully created post here.
          console.log('Post created:', post);
        }),

        catchError((error: unknown) => {
          this.error.set(this.getErrorMessage(error));
          return EMPTY;
        }),

        finalize(() => this.loading.set(false)),
      )
      .subscribe();
  }

  private getErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 0) {
        return 'Cannot connect to the server.';
      }

      if (error.status === 401) {
        return 'Your session has expired or you are not authenticated.';
      }

      if (error.status === 403) {
        return 'You do not have permission to perform this action.';
      }

      const message = error.error?.message;

      if (typeof message === 'string') {
        return message;
      }
    }

    if (error instanceof Error) {
      return error.message;
    }

    return 'An unexpected error occurred. Please try again.';
  }
}