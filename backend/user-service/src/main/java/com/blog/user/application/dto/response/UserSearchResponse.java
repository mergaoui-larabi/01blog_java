package com.blog.user.application.dto.response;

import java.time.Instant;
import java.util.UUID;
import com.blog.user.domain.model.UserRole;

public record UserSearchResponse(
                UUID id,
                String email,
                String username,
                Short age,
                String gender,
                UserRole role,
                UUID avatarMediaId,
                Boolean banned,
                Boolean followedByCurrentUser,
                Instant createdAt,
                Instant updatedAt) {
}