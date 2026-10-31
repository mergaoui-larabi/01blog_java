package com.blog.user.application.dto.response;

import java.util.UUID;

import com.blog.user.domain.model.UserRole;

public record UserRoleResponse(
        UUID userId,
        UserRole role,
        Boolean banned) {
}
