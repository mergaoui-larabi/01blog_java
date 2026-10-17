package com.blog.post.application.dto.request;

import java.util.UUID;

public record PostLikeCreateRequest(
        UUID postId,
        UUID userId) {
}
