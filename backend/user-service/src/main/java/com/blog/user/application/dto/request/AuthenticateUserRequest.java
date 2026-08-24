package com.blog.user.application.dto.request;

public record AuthenticateUserRequest(
        String email,
        String password) {
}