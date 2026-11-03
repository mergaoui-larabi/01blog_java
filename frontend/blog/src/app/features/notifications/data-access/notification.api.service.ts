import { inject, Service } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environments';

export type NotificationType = 'NEW_POST';

export interface Notification {
  id: string;
  recipientId: string;
  actorId: string;
  type: NotificationType;
  resourceId: string | null;
  message: string | null;
  read: boolean;
  readAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface PageResponse<T> {
  content: T[];
  nextCursor: string | null;
  hasNext: boolean;
}

export interface UnreadCountResponse {
  unreadCount: number;
}

@Service()
export class NotificationApiService {
  private readonly http = inject(HttpClient);

  private readonly env = environment;

  fetchNotifications(cursor?: string | null, size = 20): Observable<PageResponse<Notification>> {
    let params = new HttpParams().set('size', size);
    if (cursor) {
      params = params.set('cursor', cursor);
    }

    const url = this.env.getNotificationsEndpoint.split('?')[0];

    return this.http.get<PageResponse<Notification>>(url, { params });
  }

  fetchUnreadCount(): Observable<UnreadCountResponse> {
    return this.http.get<UnreadCountResponse>(this.env.getUnreadCountEndpoint);
  }

  setRead(notificationId: string, read: boolean): Observable<Notification> {
    const url = this.env.patchNotificationReadEndpoint.replace(
      '{notificationId}',
      encodeURIComponent(notificationId),
    );

    return this.http.patch<Notification>(url, { read });
  }

  setAllRead(): Observable<void> {
    return this.http.patch<void>(this.env.patchAllNotificationsReadEndpoint, {});
  }
}
