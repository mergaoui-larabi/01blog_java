package com.blog.post.application.dto.request;

import java.util.List;

public record UpdatePostRequest(
        String content,
        List<String> mediaIds,
        Boolean hidden) {
}
