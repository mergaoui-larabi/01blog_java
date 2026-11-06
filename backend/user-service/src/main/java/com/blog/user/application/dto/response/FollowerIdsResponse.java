package com.blog.user.application.dto.response;

import java.util.List;
import java.util.UUID;

public record FollowerIdsResponse(
        UUID userId,
        List<UUID> followerIds) {
}
