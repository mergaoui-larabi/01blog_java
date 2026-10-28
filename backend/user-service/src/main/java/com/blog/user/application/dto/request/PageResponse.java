package com.blog.user.application.dto.request;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        String nextCursor) {
}