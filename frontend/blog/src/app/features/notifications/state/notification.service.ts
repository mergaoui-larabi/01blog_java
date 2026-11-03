import { HttpErrorResponse } from '@angular/common/http';
import { inject, Service, signal } from '@angular/core';
import { EMPTY } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';

import { AlertService } from '../../../core/alerts/alert.service';
import { Notification, NotificationApiService } from '../data-access/notification.api.service';

@Service()
export class NotificationService {
  private readonly alertService = inject(AlertService);
  private readonly notificationApi = inject(NotificationApiService);

  readonly notifications = signal<Notification[]>([]);
  readonly loading = signal(false);
  readonly nextCursor = signal<string | null>(null);
  readonly hasNext = signal(false);
  readonly unreadCount = signal(0);

  private readonly pendingFlags = signal<Record<string, boolean>>({});

  load(): void {
    this.fetchPage(null, false);
  }

  loadMore(): void {
    if (this.loading() || !this.hasNext()) return;

    this.fetchPage(this.nextCursor(), true);
  }

  private fetchPage(cursor: string | null, append: boolean): void {
    if (this.loading()) return;

    this.loading.set(true);

    this.notificationApi
      .fetchNotifications(cursor)
      .pipe(
        catchError((error: unknown) => {
          // Without this the button stays enabled and re-issues the same failing request.
          this.hasNext.set(false);
          this.handleError(error, 'Failed to load notifications.');
          return EMPTY;
        }),
        finalize(() => this.loading.set(false)),
      )
      .subscribe((page) => {
        this.notifications.update((current) => {
          if (!append) return page.content;

          const existingIds = new Set(current.map((notification) => notification.id));
          return [
            ...current,
            ...page.content.filter((notification) => !existingIds.has(notification.id)),
          ];
        });

        this.nextCursor.set(page.nextCursor);
        this.hasNext.set(page.hasNext);
      });
  }

  loadUnreadCount(): void {
    this.notificationApi
      .fetchUnreadCount()
      .pipe(
        // The header badge is background chrome; a failure there should not interrupt the user.
        catchError(() => EMPTY),
      )
      .subscribe((response) => this.unreadCount.set(response.unreadCount));
  }

  /** Marks read without flipping an already read notification back to unread. */
  markRead(notification: Notification): void {
    if (notification.read) return;

    this.setRead(notification.id, true);
  }

  toggleRead(notification: Notification): void {
    this.setRead(notification.id, !notification.read);
  }

  setAllRead(): void {
    this.notificationApi
      .setAllRead()
      .pipe(
        catchError((error: unknown) => {
          this.handleError(error, 'Failed to mark notifications as read.');
          return EMPTY;
        }),
      )
      .subscribe(() => {
        this.notifications.update((notifications) =>
          notifications.map((notification) =>
            notification.read ? notification : { ...notification, read: true },
          ),
        );
        this.unreadCount.set(0);
        this.alertService.success('All notifications marked as read.');
      });
  }

  readPending(notificationId: string): boolean {
    return Boolean(this.pendingFlags()[notificationId]);
  }

  private setRead(notificationId: string, read: boolean): void {
    if (this.pendingFlags()[notificationId]) return;

    this.pendingFlags.update((flags) => ({ ...flags, [notificationId]: true }));

    this.notificationApi
      .setRead(notificationId, read)
      .pipe(
        catchError((error: unknown) => {
          this.handleError(error, 'Failed to update notification.');
          return EMPTY;
        }),
        finalize(() =>
          this.pendingFlags.update((flags) => {
            const next = { ...flags };
            delete next[notificationId];

            return next;
          }),
        ),
      )
      .subscribe((updated) => {
        const previousRead = this.notifications().find(
          (notification) => notification.id === updated.id,
        )?.read;

        this.notifications.update((notifications) =>
          notifications.map((notification) =>
            notification.id === updated.id ? { ...notification, ...updated } : notification,
          ),
        );

        if (previousRead !== undefined && previousRead !== updated.read) {
          this.unreadCount.update((count) => count + (updated.read ? -1 : 1));
        }
      });
  }

  private handleError(error: unknown, fallback: string): void {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 0) {
        this.alertService.error('Cannot connect to the server.');
        return;
      }

      this.alertService.error(error.error?.message ?? fallback);
      return;
    }

    this.alertService.error('An unexpected error occurred. Please try again.');
  }
}
