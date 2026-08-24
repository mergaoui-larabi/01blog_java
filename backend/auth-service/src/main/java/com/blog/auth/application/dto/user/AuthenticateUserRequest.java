package com.blog.auth.application.dto.user;

public record AuthenticateUserRequest(
        String email,
        String password) {
}