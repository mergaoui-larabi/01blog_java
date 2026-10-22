package com.blog.post.client.dto;

import java.util.List;
import java.util.UUID;

public record FollowedUsersResponse(
        UUID userId,
        List<UUID> followedUserIds) {
}
