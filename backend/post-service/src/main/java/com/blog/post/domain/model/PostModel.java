package com.blog.post.domain.model;

import com.blog.post.domain.entity.Post;
import com.blog.post.domain.valueobject.PostContent;
import com.blog.post.domain.valueobject.LikesCount;
import com.blog.post.domain.valueobject.CommentsCount;

import java.time.Instant;
import java.util.UUID;

public record PostModel(
        UUID id,
        UUID authorId,
        String content,
        long likesCount,
        long commentsCount,
        boolean visible,
        Instant createdAt,
        Instant updatedAt) {

    public static PostModel from(Post post) {
        return new PostModel(
                post.getId(),
                post.getAuthorId(),
                post.getContent().value(),
                post.getLikesCount().value(),
                post.getCommentsCount().value(),
                post.getVisible() != null && post.getVisible(),
                post.getCreatedAt(),
                post.getUpdatedAt());
    }
}
