package com.blog.auth.application.dto.request;

public record LoginRequest(
        String email,
        String password) {
}