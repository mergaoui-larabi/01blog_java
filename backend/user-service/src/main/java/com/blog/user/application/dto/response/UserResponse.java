package com.blog.user.application.dto.response;

import java.time.Instant;
import java.util.UUID;
import com.blog.user.domain.model.UserRole;

public record UserResponse(
                UUID id,
                String email,
                String username,
                Short age,
                String gender,
                UserRole role,
                UUID avatarMediaId,
                Instant createdAt,
                Instant updatedAt) {
}