package com.blog.post.event;

import java.util.UUID;

/**
 * Local, in-process event published inside the post creation transaction. Handled only after that
 * transaction commits, so a rolled back post never produces notifications.
 */
public record PostCreatedEvent(
        UUID postId,
        UUID authorId) {
}
