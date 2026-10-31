package com.blog.moderation.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateUserReportRequest(
        @NotNull(message = "user id is required") UUID userId,
        @NotBlank(message = "reason must not be blank")
        @Size(max = 2000, message = "reason must be at most 2000 characters") String reason) {
}
