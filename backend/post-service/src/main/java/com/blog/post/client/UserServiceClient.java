package com.blog.post.client;

import java.util.List;
import java.util.UUID;

public interface UserServiceClient {
    List<UUID> getFollowedUserIds(UUID userId);
}
