package com.blog.auth.application.dto.user;

import java.util.UUID;

public record UserAuthenticationResponse(
        UUID userId,
        String role) {
}
