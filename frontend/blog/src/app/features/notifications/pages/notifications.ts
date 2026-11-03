import { Component, inject } from '@angular/core';
import { NotificationItem } from '../components/notification-item/notification-item';
import { NotificationService } from '../state/notification.service';

@Component({
  selector: 'app-notifications',
  imports: [NotificationItem],
  templateUrl: './notifications.html',
  styleUrl: './notifications.scss',
})
export class Notifications {
  protected readonly notificationService = inject(NotificationService);

  ngOnInit(): void {
    this.notificationService.load();
    this.notificationService.loadUnreadCount();
  }
}
