package com.blog.moderation.application.dto.response;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        String nextCursor,
        boolean hasNext) {
}
