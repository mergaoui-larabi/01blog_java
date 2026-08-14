package com.blog.gateway.exception;

import java.time.Instant;

public record GatewayErrorResponse(
        Instant timestamp,
        int status,
        GatewayErrorCode code,
        String message,
        String path,
        String requestId) {
}
