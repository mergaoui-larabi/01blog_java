package com.blog.user.application.dto.request;

public record CreateUserRequest(
                String email,
                String username,
                String password,
                Short age,
                String gender) {
}