package com.blog.notification.application.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PostCreatedNotificationRequest(
        @NotNull(message = "post id is required") UUID postId,
        @NotNull(message = "author id is required") UUID authorId) {
}
