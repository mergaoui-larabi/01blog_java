import { HttpErrorResponse } from '@angular/common/http';
import { computed, inject, Service, signal } from '@angular/core';
import { EMPTY } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';

import { ExploreApiService, UserSearch } from '../data-access/explore.api.service';
import { AlertService } from '../../../core/alerts/alert.service';

@Service()
export class ExploreService {
  private readonly alertService = inject(AlertService);
  private readonly exploreApi = inject(ExploreApiService);

  // State
  readonly users = signal<UserSearch[]>([]);
  readonly loading = signal(false);
  readonly searchTerm = signal('');
  readonly nextCursor = signal<string | null>(null);
  private readonly cursorCreatedAt = signal<string | null>(null);

  // Derived state
  readonly hasUsers = computed(() => this.users().length > 0);

  followUser(userId: string): void {
    this.exploreApi.followUser(userId).subscribe({
      next: () => this.updateFollowState(userId, true),
      error: (error: unknown) => {
        this.getErrorMessage(error);
        this.updateFollowState(userId, false);
      },
    });
  }

  unfollowUser(userId: string): void {
    this.exploreApi.unfollowUser(userId).subscribe({
      next: () => this.updateFollowState(userId, false),
      error: (error: unknown) => {
        this.getErrorMessage(error);
        this.updateFollowState(userId, true);
      },
    });
  }

  loadUsers(): void {
    this.fetchUsers(null, null, false);
  }

  loadMore(): void {
    if (this.loading()) return;

    const cursorId = this.nextCursor();
    const cursorInstant = this.cursorCreatedAt();

    if (!cursorId || !cursorInstant) return;

    this.fetchUsers(cursorId, cursorInstant, true);
  }

  searchUsers(term: string): void {
    const normalizedTerm = term.trim();

    if (normalizedTerm === this.searchTerm()) return;

    this.searchTerm.set(normalizedTerm);
    this.users.set([]);
    this.nextCursor.set(null);
    this.cursorCreatedAt.set(null);

    this.fetchUsers(null, null, false);
  }

  updateFollowState(userId: string, followed: boolean): void {
    this.users.update((users) =>
      users.map((user) =>
        user.id === userId ? { ...user, followedByCurrentUser: followed } : user,
      ),
    );
  }

  private fetchUsers(cursorId: string | null, cursorInstant: string | null, append: boolean): void {
    if (this.loading()) return;

    this.loading.set(true);

    this.exploreApi
      .fetchUsers(cursorId, cursorInstant, this.searchTerm())
      .pipe(
        catchError((error: unknown) => {
          this.getErrorMessage(error);
          return EMPTY;
        }),
        finalize(() => this.loading.set(false)),
      )
      .subscribe((page) => {
        this.users.update((currentUsers) => {
          if (!append) return page.content;

          const existingIds = new Set(currentUsers.map((user) => user.id));
          const newUsers = page.content.filter((user) => !existingIds.has(user.id));

          return [...currentUsers, ...newUsers];
        });

        this.nextCursor.set(page.nextCursor);

        // Assumes nextCursor identifies the last returned user.
        // If the API returns an opaque cursor, use that instead.
        const lastUser = page.content.at(-1);

        this.cursorCreatedAt.set(page.nextCursor && lastUser ? lastUser.createdAt : null);
      });
  }

  private getErrorMessage(error: unknown): void {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 0) {
        this.alertService.error('Cannot connect to the server.');
        return;
      }

      const message = error.error?.message;

      this.alertService.error(
        typeof message === 'string' ? message : 'Unable to load users. Please try again.',
      );

      return;
    }

    this.alertService.error('An unexpected error occurred. Please try again.');
  }
}
