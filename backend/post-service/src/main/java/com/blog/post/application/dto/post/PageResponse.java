package com.blog.post.application.dto.post;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        String nextCursor,
        boolean hasNext) {
}
