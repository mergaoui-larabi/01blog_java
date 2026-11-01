package com.blog.notification.application.dto.response;

import com.blog.notification.domain.entity.Notification;
import com.blog.notification.domain.model.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID recipientId,
        UUID actorId,
        NotificationType type,
        UUID resourceId,
        String message,
        boolean read,
        Instant readAt,
        Instant createdAt,
        Instant updatedAt) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getRecipientId(),
                notification.getActorId(),
                notification.getType(),
                notification.getResourceId(),
                notification.getMessage(),
                notification.isRead(),
                notification.getReadAt(),
                notification.getCreatedAt(),
                notification.getUpdatedAt());
    }
}
