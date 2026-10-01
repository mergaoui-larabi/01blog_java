package com.blog.auth.application.dto.error;

import java.time.Instant;

public record AuthErrorResponse(
        Instant timestamp,
        String status,
        int code,
        String message,
        String path) {
}
