package com.blog.post.domain.model;

import com.blog.post.domain.entity.Comment;

import java.time.Instant;
import java.util.UUID;

public record CommentModel(
        UUID id,
        UUID postId,
        UUID authorId,
        String content,
        Instant createdAt,
        Instant updatedAt) {

    public static CommentModel from(Comment comment) {
        return new CommentModel(
                comment.getId(),
                comment.getPostId(),
                comment.getAuthorId(),
                comment.getContent().value(),
                comment.getCreatedAt(),
                comment.getUpdatedAt());
    }
}
