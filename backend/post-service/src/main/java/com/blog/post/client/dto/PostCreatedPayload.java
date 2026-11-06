package com.blog.post.client.dto;

import java.util.UUID;

public record PostCreatedPayload(
        UUID postId,
        UUID authorId) {
}
