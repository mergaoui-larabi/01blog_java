package com.blog.post.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record HidePostRequest(
        @NotNull Boolean hidden) {
}
