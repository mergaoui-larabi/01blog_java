package com.blog.user.application.dto.request;

import java.util.UUID;

public record CreateUserRequest(
        String email,
        String username,
        String password,
        Short age,
        String gender,
        UUID avatarMediaId) {
}