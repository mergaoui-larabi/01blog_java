package com.blog.media.application.dto.error;

import java.time.Instant;

public record BlogErrorResponse(
                Instant timestamp,
                String status,
                int code,
                String message,
                String path) {
}