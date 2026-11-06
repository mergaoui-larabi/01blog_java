package com.blog.moderation.application.dto.response;

import java.util.UUID;

public record UserCleanupResponse(
        UUID userId,
        long reportsAnonymized,
        long reportsClosed) {
}
