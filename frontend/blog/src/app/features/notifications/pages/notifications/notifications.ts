import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NotificationsService } from '../../data-access/notifications.service';
import { AppNotification, NotificationType } from '../../models/notification.model';
import { Avatar } from '../../../../shared/ui/avatar/avatar';
import { TimeAgoPipe } from '../../../../shared/pipes/time-ago.pipe';

const TYPE_LABELS: Record<NotificationType, string> = {
  NEW_POST: 'published a new post',
  POST_LIKED: 'liked your post',
  COMMENT_ADDED: 'commented on your post',
  NEW_SUBSCRIBER: 'subscribed to your Block',
};

/**
 * Notification center with read/unread states. Backed by local mock state;
 * swap NotificationsService for a REST/polling implementation later.
 */
@Component({
  selector: 'app-notifications',
  templateUrl: './notifications.html',
  imports: [RouterLink, Avatar, TimeAgoPipe],
})
export class Notifications {
  private readonly notificationsService = inject(NotificationsService);

  protected readonly notifications = this.notificationsService.notifications;
  protected readonly unreadCount = this.notificationsService.unreadCount;

  protected labelFor(notification: AppNotification): string {
    return TYPE_LABELS[notification.type];
  }

  /** Post-related notifications link to the post's author Block. */
  protected linkFor(notification: AppNotification): string[] {
    if (notification.type === 'NEW_SUBSCRIBER') {
      return ['/block', notification.actor.username];
    }

    // Posts live on their author's Block in this phase.
    return ['/block', notification.actor.username];
  }

  protected markRead(notification: AppNotification): void {
    if (!notification.read) {
      this.notificationsService.markRead(notification.id);
    }
  }

  protected markAllRead(): void {
    this.notificationsService.markAllRead();
  }
}
