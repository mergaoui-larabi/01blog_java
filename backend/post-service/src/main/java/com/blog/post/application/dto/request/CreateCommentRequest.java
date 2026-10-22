package com.blog.post.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
        @NotBlank(message = "Comment content cannot be blank")
        @Size(max = 2000, message = "Comment content must not exceed 2000 characters")
        String content) {
}
