package com.blog.post.application.dto.request;

import java.util.UUID;

public record CommentCreateRequest(
        UUID postId,
        UUID authorId,
        String content,
        UUID parentCommentId) {
}
