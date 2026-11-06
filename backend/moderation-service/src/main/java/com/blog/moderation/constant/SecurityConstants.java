package com.blog.moderation.constant;

public final class SecurityConstants {

    private SecurityConstants() {
    }

    // Role value returned by User Service for the caller's current account
    // (auth-service issues a "role" claim too, but it is stale by design and never authorizes moderation work)
    public static final String ADMIN_ROLE = "ADMIN";

    public static final String REQUEST_ID_HEADER = "X-Request-ID";

    // Service-to-service authentication for /internal/** endpoints (see InternalTokenFilter)
    public static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";
    public static final String INTERNAL_SERVICE_AUTHORITY = "INTERNAL_SERVICE";
    public static final String INTERNAL_SERVICE_PRINCIPAL = "internal-service";
}
