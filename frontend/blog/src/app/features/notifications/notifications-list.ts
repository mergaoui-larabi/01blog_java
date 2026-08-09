import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Notification } from '../../core/models/notification.model';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-notifications-list',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './notifications-list.html',
  styleUrl: './notifications-list.css',
})
export class NotificationsList implements OnInit {
  private readonly notificationService = inject(NotificationService);

  notifications: Notification[] = [];
  isLoading = true;

  ngOnInit(): void {
    this.loadNotifications();
  }

  private loadNotifications(): void {
    this.isLoading = true;
    this.notificationService.getNotifications().subscribe({
      next: (notifications) => {
        this.notifications = notifications;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  markAsRead(notification: Notification): void {
    if (notification.isRead) return;
    this.notificationService.markAsRead(notification.id).subscribe({
      next: () => {
        notification.isRead = true;
      },
    });
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead().subscribe({
      next: () => {
        this.notifications.forEach((n) => (n.isRead = true));
      },
    });
  }

  getNotificationIcon(type: string): string {
    switch (type) {
      case 'NEW_POST': return '📝';
      case 'SUBSCRIBE': return '👥';
      case 'LIKE': return '❤️';
      case 'COMMENT': return '💬';
      default: return '🔔';
    }
  }

  getNotificationLink(notification: Notification): any[] {
    if (notification.relatedPostId) {
      return ['/posts', notification.relatedPostId.toString()];
    }
    if (notification.relatedUsername) {
      return ['/profile', notification.relatedUsername];
    }
    return ['/'];
  }

  getRelativeTime(date: string): string {
    const now = new Date();
    const notifDate = new Date(date);
    const diffMs = now.getTime() - notifDate.getTime();
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));

    if (diffHours < 1) {
      const diffMins = Math.floor(diffMs / (1000 * 60));
      return diffMins <= 1 ? 'just now' : `${diffMins} min ago`;
    }
    if (diffHours < 24) return `${diffHours} hours ago`;
    const diffDays = Math.floor(diffHours / 24);
    return `${diffDays} days ago`;
  }
}
