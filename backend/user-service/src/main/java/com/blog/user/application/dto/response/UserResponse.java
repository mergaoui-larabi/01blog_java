package com.blog.user.application.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.blog.user.domain.model.Gender;
import com.blog.user.domain.model.UserRole;

public record UserResponse(
        UUID id,
        String email,
        String username,
        Short age,
        Gender gender,
        UserRole role,
        Instant createdAt,
        Instant updatedAt) {
}