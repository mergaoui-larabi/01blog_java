import { HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { EMPTY } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';

import { FeedApiService, Post } from '../data-access/feed.api.service';
import { AlertService } from '../../../core/alerts/alert.service';

@Injectable({ providedIn: 'root' })
export class FeedService {
  private readonly alertService = inject(AlertService);
  private readonly feedApi = inject(FeedApiService);

  readonly loading = signal(false);
  readonly posts = signal<Post[]>([]);
  readonly nextCursor = signal<string | null>(null);

  // The server sends hasNext=false on the last page. Without it, loadMore() would call
  // fetchFeed(null), and a null cursor is indistinguishable from a first request, so the
  // server would answer with the first page again.
  readonly hasNext = signal(false);

  loadFeed(): void {
    this.fetchPage(null, false);
  }

  loadMore(): void {
    if (this.loading() || !this.hasNext()) return;

    this.fetchPage(this.nextCursor(), true);
  }

  private fetchPage(cursor: string | null, append: boolean): void {
    if (this.loading()) return;

    this.loading.set(true);

    this.feedApi
      .fetchFeed(cursor)
      .pipe(
        catchError((error: unknown) => {
          // Stop auto-loading after a failed page. The sentinel re-arms as soon as the
          // list lands, so leaving hasNext true would retry the same request endlessly.
          this.hasNext.set(false);
          this.getErrorMessage(error);
          return EMPTY;
        }),
        finalize(() => this.loading.set(false)),
      )
      .subscribe((page) => {
        this.posts.update((currentPosts) => {
          if (!append) return page.content;

          const existingIds = new Set(currentPosts.map((post) => post.id));
          const newPosts = page.content.filter((post) => !existingIds.has(post.id));

          return [...currentPosts, ...newPosts];
        });

        this.nextCursor.set(page.nextCursor);
        this.hasNext.set(page.hasNext);
      });
  }

  addPostToFeed(post: Post): void {
    this.posts.update((posts) => [post, ...posts]);
  }

  patchPost(postId: string, patch: Partial<Post>): void {
    this.posts.update((posts) =>
      posts.map((post) => (post.id === postId ? { ...post, ...patch } : post)),
    );
  }

  removePost(postId: string): void {
    this.posts.update((posts) => posts.filter((post) => post.id !== postId));
  }

  private getErrorMessage(error: unknown): void {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 0) {
        this.alertService.error('Cannot connect to the server.');
        return;
      }

      this.alertService.error(error.error?.message ?? 'Failed to load posts. Please try again.');
      return;
    }

    this.alertService.error('An unexpected error occurred. Please try again.');
  }
}
