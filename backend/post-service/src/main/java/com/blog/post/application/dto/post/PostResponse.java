package com.blog.post.application.dto.post;

import java.time.Instant;
import java.util.List;

public record PostResponse(
        String id,
        String authorId,
        String content,
        List<String> mediaIds,
        long likesCount,
        long commentsCount,
        boolean likedByCurrentUser,
        boolean hidden,
        Instant createdAt,
        Instant updatedAt) {
}
