package com.blog.post.application.dto;

import java.util.UUID;

public record UserCleanupResponse(
        UUID userId,
        long postsAnonymized,
        long commentsAnonymized,
        long likesRemoved) {
}
