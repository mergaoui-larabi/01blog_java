package com.blog.user.application.dto.response;

import java.util.List;
import java.util.UUID;

public record FollowedUsersResponse(
        UUID userId,
        List<UUID> followedUserIds) {
}
