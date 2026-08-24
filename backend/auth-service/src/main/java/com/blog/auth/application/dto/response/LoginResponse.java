package com.blog.auth.application.dto.response;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn) {
}
