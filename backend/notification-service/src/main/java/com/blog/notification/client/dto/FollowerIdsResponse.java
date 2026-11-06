package com.blog.notification.client.dto;

import java.util.List;
import java.util.UUID;

public record FollowerIdsResponse(
        UUID userId,
        List<UUID> followerIds) {
}
