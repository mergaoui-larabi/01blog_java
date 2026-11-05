import { HttpErrorResponse } from '@angular/common/http';
import { inject, Service, signal } from '@angular/core';
import { EMPTY } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';

import { AlertService } from '../../../core/alerts/alert.service';
import { Notification, NotificationApiService } from '../data-access/notification.api.service';
import {
  NotificationSocketService,
  RemoteNotificationEvent,
} from '../data-access/notification-socket.service';

@Service()
export class NotificationService {
  private readonly alertService = inject(AlertService);
  private readonly notificationApi = inject(NotificationApiService);
  private readonly socket = inject(NotificationSocketService);

  readonly notifications = signal<Notification[]>([]);
  readonly loading = signal(false);
  readonly nextCursor = signal<string | null>(null);
  readonly hasNext = signal(false);
  readonly unreadCount = signal(0);

  private readonly pendingFlags = signal<Record<string, boolean>>({});

  constructor() {
    this.socket.notification$.subscribe((notification) => this.receive(notification));
    this.socket.remote$.subscribe((event) => this.syncRemote(event));
  }

  /**
   * A pushed row can already be on screen, either because the page loaded after the event was raised or because
   * the same delivery reached this tab twice, so only a genuinely new row is allowed to move the badge.
   */
  private receive(notification: Notification): void {
    if (this.notifications().some((row) => row.id === notification.id)) return;

    this.notifications.update((rows) => [notification, ...rows]);
    if (!notification.read) this.unreadCount.update((count) => count + 1);
  }

  /** Applies a change another tab made, which is why this must stay idempotent. */
  private syncRemote(event: RemoteNotificationEvent): void {
    if (event.kind === 'read-all') {
      this.applyAllRead();
      return;
    }

    this.applyReadState(event.notificationId, event.read);
  }

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
        this.applyAllRead();
        this.socket.publishAllRead();
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
        this.applyReadState(updated.id, updated.read, updated);
        this.socket.publishRead(updated.id, updated.read);
      });
  }

  /**
   * The single place a read-state change lands, so the change this tab made and the same change reported by
   * another tab cannot move the badge twice. A row that is not loaded here still counts: the badge covers every
   * notification, not only the page that happens to be on screen.
   */
  private applyReadState(notificationId: string, read: boolean, updated?: Notification): void {
    const current = this.notifications().find((notification) => notification.id === notificationId);
    if (current?.read === read) return;

    if (current) {
      this.notifications.update((rows) =>
        rows.map((row) => (row.id === notificationId ? { ...row, ...(updated ?? { read }) } : row)),
      );
    }

    this.unreadCount.update((count) => Math.max(0, count + (read ? -1 : 1)));
  }

  private applyAllRead(): void {
    this.notifications.update((rows) =>
      rows.map((row) => (row.read ? row : { ...row, read: true })),
    );
    this.unreadCount.set(0);
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
