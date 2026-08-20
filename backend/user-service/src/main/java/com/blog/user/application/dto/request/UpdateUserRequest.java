package com.blog.user.application.dto.request;

import com.blog.user.domain.model.Gender;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public record UpdateUserRequest(
        @Email @Size(max = 255) String email,
        @Size(min = 3, max = 100) String username,
        @Size(min = 8, max = 100) String password,
        @Min(13) @Max(120) Short age,
        Gender gender) {
}