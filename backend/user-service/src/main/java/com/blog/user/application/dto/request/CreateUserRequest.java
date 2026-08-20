package com.blog.user.application.dto.request;

import com.blog.user.domain.model.Gender;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(
                @NotBlank @Email @Size(max = 255) String email,
                @NotBlank @Size(min = 3, max = 100) String username,
                @NotBlank @Size(min = 8, max = 100) String password,
                @NotNull @Min(13) @Max(120) Short age,
                @NotNull Gender gender) {
}