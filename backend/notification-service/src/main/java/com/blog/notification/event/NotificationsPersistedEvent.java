package com.blog.notification.event;

import com.blog.notification.application.dto.response.NotificationResponse;

import java.util.List;

/**
 * Local, in-process event published inside the notification persistence transaction and handled only
 * after that transaction commits, so nothing reaches a websocket subscriber before it is stored.
 */
public record NotificationsPersistedEvent(
        List<NotificationResponse> notifications) {
}
