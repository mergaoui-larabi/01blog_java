package com.blog.notification.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateNotificationsRequest(
        @NotEmpty(message = "at least one notification is required")
        @Size(max = 100, message = "at most 100 notifications can be created at once")
        List<@Valid CreateNotificationRequest> notifications) {
}
