package com.blog.auth.constant;

public final class SecurityConstants {

    private SecurityConstants() {
    }

    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String REQUEST_ID_ATTRIBUTE = "requestId";

    // Service-to-service authentication: sent on User Service's /internal/** endpoints
    public static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";
}
