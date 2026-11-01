package com.blog.notification.application.dto.request;

import com.blog.notification.domain.model.NotificationType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateNotificationRequest(
        @NotNull(message = "recipient id is required") UUID recipientId,
        @NotNull(message = "actor id is required") UUID actorId,
        @NotNull(message = "type is required") NotificationType type,
        UUID resourceId,
        @Size(max = 2000, message = "message must be at most 2000 characters") String message) {
}
