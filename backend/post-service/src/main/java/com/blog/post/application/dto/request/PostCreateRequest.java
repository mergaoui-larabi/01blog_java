package com.blog.post.application.dto.request;

import java.util.UUID;

public record PostCreateRequest(
        UUID authorId,
        String content) {
}
