package com.blog.notification.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateNotificationReadRequest(
        @NotNull(message = "read must be true or false") Boolean read) {
}
