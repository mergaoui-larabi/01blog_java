package com.blog.user.application.dto.error;

import java.time.Instant;

public record UserErrorResponse(
                Instant timestamp,
                String status,
                int code,
                String message,
                String path) {
}