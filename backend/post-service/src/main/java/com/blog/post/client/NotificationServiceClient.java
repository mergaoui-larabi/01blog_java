package com.blog.post.client;

import java.util.UUID;

public interface NotificationServiceClient {
    void notifyPostCreated(UUID postId, UUID authorId);
}
