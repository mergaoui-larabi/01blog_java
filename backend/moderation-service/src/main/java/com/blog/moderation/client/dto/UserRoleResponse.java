package com.blog.moderation.client.dto;

import java.util.UUID;

public record UserRoleResponse(
        UUID userId,
        String role,
        Boolean banned) {
}
