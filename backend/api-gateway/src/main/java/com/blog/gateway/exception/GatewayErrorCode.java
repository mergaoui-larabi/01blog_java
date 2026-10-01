package com.blog.gateway.exception;

public enum GatewayErrorCode {
    ROUTE_NOT_FOUND,
    BAD_REQUEST,
    UNAUTHORIZED,
    FORBIDDEN,
    SERVICE_UNAVAILABLE,
    GATEWAY_TIMEOUT,
    INTERNAL_SERVER_ERROR
}
