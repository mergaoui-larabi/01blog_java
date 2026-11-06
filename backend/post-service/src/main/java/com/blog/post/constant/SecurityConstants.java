package com.blog.post.constant;

public final class SecurityConstants {

    private SecurityConstants() {
    }

    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String REQUEST_ID_ATTRIBUTE = "requestId";

    // JWT claims (see auth-service JwtConstants: sub = userId, role = ADMIN|USER)
    public static final String ROLE_CLAIM = "role";
    public static final String ADMIN_ROLE = "ADMIN";

    // Service-to-service authentication for /internal/** endpoints (see InternalTokenFilter)
    public static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";
    public static final String INTERNAL_SERVICE_AUTHORITY = "INTERNAL_SERVICE";
    public static final String INTERNAL_SERVICE_PRINCIPAL = "internal-service";
}
