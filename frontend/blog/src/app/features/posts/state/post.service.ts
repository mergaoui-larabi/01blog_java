import { HttpErrorResponse } from '@angular/common/http';
import { inject, Service, signal } from '@angular/core';
import { EMPTY, from, Observable, throwError } from 'rxjs';
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
  Post,
  PostApiService,
  UpdatePostRequest,
} from '../data-access/post.api.service';
import { AlertService } from '../../../core/alerts/alert.service';
import { Router } from '@angular/router';
import { FeedService } from '../../feed/state/feed.service';
import { BlockService } from '../../block/state/block.service';

@Service()
export class PostService {
  private readonly alertService = inject(AlertService);
  private readonly postApi = inject(PostApiService);
  private readonly router = inject(Router);
  private readonly feedService = inject(FeedService);
  private readonly blockService = inject(BlockService);

  readonly lastPost = signal<Post | null>(null);
  readonly loading = signal(false);

  /** Tracks which posts currently have an in-flight like/unlike request. */
  readonly pendingLikes = signal<Record<string, boolean>>({});

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
      switchMap((mediaIds) => this.postApi.createPost({ content, mediaIds } satisfies CreatePostRequest)),
      tap(() => {
        this.router.navigate(['/block/me']);
        this.alertService.success('Post published!');
      }),
      catchError((error: unknown) => {
        this.getErrorMessage(error);
        return EMPTY;
      }),
    );
  }

  toggleLike(post: Post, onSettled?: (updated: Post) => void): void {
    const postId = post.id;

    // Prevent duplicate requests while one is in flight.
    if (this.pendingLikes()[postId]) {
      return;
    }

    const liked = post.likedByCurrentUser;
    const nextCount = liked ? Math.max(0, post.likesCount - 1) : post.likesCount + 1;
    this.applyToAllLists(postId, { likedByCurrentUser: !liked, likesCount: nextCount });
    this.pendingLikes.update((pending) => ({ ...pending, [postId]: true }));

    const request = liked ? this.postApi.unlikePost(postId) : this.postApi.likePost(postId);

    request.subscribe({
      next: () => {
        this.pendingLikes.update((pending) => ({ ...pending, [postId]: false }));
        const updated = { ...post, likedByCurrentUser: !liked, likesCount: nextCount };
        onSettled?.(updated);
      },
      error: (error: unknown) => {
        // Roll the optimistic change back.
        this.applyToAllLists(postId, { likedByCurrentUser: liked, likesCount: post.likesCount });
        this.pendingLikes.update((pending) => ({ ...pending, [postId]: false }));
        this.getErrorMessage(error);
        onSettled?.(post);
      },
    });
  }

  /**
   * Edits a post via PATCH so untouched fields (e.g. media) are preserved.
   * New files are uploaded first; mediaIds are only sent when the user
   * added or removed attachments.
   */
  savePostEdits(
    post: Post,
    content: string,
    newFiles: File[],
    mediaIds: string[],
    mediaChanged: boolean,
  ): Observable<Post> {
    const uploads = from(newFiles).pipe(
      concatMap((file) =>
        this.postApi.uploadMedia([file]).pipe(
          map((responses) => {
            const uploaded = responses[0];

            if (!uploaded?.id) {
              throw new Error(`The media service did not return an ID for "${file.name}".`);
            }

            return uploaded.id;
          }),
        ),
      ),
      toArray(),
    );

    return uploads.pipe(
      switchMap((uploadedIds) => {
        const request: UpdatePostRequest = { content };

        if (mediaChanged || uploadedIds.length > 0) {
          request.mediaIds = [...mediaIds, ...uploadedIds];
        }

        return this.postApi.updatePost(post.id, request);
      }),
      tap((updated) => {
        this.applyToAllLists(post.id, updated);
        this.alertService.success('Post updated.');
      }),
      catchError((error: unknown) => {
        this.getErrorMessage(error);
        // Re-throw so the caller can reset its saving state and keep edits.
        return throwError(() => error);
      }),
    );
  }

  deletePost(postId: string): void {
    this.postApi.deletePost(postId).subscribe({
      next: () => {
        this.feedService.removePost(postId);
        this.blockService.removePost(postId);
        this.alertService.success('Post deleted.');
      },
      error: (error: unknown) => this.getErrorMessage(error),
    });
  }

  changeCommentCount(postId: string, delta: number, fallbackCount?: number): void {
    const current = this.feedService.posts().find((post) => post.id === postId)
      ?? this.blockService.posts().find((post) => post.id === postId);
    const previous = current?.commentsCount ?? fallbackCount ?? 0;
    const next = Math.max(0, previous + delta);

    this.applyToAllLists(postId, { commentsCount: next });
  }

  /** Applies a field patch (or full server response) to every visible list. */
  applyToAllLists(postId: string, patch: Partial<Post>): void {
    this.feedService.patchPost(postId, patch);
    this.blockService.patchPost(postId, patch);
  }

  getErrorMessage(error: unknown): void {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 0) {
        this.alertService.error('Cannot connect to the server.');
        return;
      }

      const message = error.error?.message;
      this.alertService.error(typeof message === 'string' ? message : 'Something went wrong. Please try again.');
      return;
    }

    this.alertService.error('An unexpected error occurred. Please try again.');
  }
}
