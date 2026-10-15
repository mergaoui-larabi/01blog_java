package com.blog.user.application.dto.request;

import java.util.UUID;

public record UpdateUserRequest(
        String email,
        String username,
        String password,
        Short age,
        String gender,
        UUID avatarMediaId) {
}