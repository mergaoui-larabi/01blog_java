package com.blog.post.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record ReplacePostRequest(
        @NotBlank(message = "Post content must not be blank")
        String content,
        List<String> mediaIds) {
}
