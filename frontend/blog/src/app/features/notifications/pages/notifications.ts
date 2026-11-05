import { Component, inject, viewChild } from '@angular/core';
import { NotificationItem } from '../components/notification-item/notification-item';
import { NotificationService } from '../state/notification.service';
import { Notification } from '../data-access/notification.api.service';
import { PostViewDialog } from '../../posts/components/post-view-dialog/post-view-dialog';

@Component({
  selector: 'app-notifications',
  imports: [NotificationItem, PostViewDialog],
  templateUrl: './notifications.html',
  styleUrl: './notifications.scss',
})
export class Notifications {
  protected readonly notificationService = inject(NotificationService);
  private readonly postViewDialog = viewChild.required(PostViewDialog);

  ngOnInit(): void {
    this.notificationService.load();
    this.notificationService.loadUnreadCount();
  }

  /**
   * resourceId is the notified post. Without one there is nothing to show, so the row is left unread rather
   * than opened and dismissed as if it had been seen.
   */
  openNotifiedPost(notification: Notification): void {
    const postId = notification.resourceId;
    if (!postId) return;

    this.notificationService.markRead(notification);
    this.postViewDialog().openById(postId);
  }
}
