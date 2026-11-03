package com.blog.user.application.dto.request;

import java.util.List;
import java.util.UUID;

public record PageResponse<T>(
                List<T> content,
                UUID nextCursor) {
}