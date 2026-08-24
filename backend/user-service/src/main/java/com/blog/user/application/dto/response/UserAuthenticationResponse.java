package com.blog.user.application.dto.response;

import java.util.UUID;

import com.blog.user.domain.model.UserRole;

public record UserAuthenticationResponse(
        UUID userId,
        UserRole role
) {
}