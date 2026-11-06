package com.blog.notification.application.dto.response;

import java.util.UUID;

public record UserCleanupResponse(
        UUID userId,
        long notificationsRemoved) {
}
