package com.blog.notification.client;

import java.util.List;
import java.util.UUID;

public interface UserServiceClient {
    List<UUID> getFollowerIds(UUID userId);
}
