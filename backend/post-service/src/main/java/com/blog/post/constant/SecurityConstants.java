package com.blog.post.constant;

public final class SecurityConstants {

    private SecurityConstants() {
    }

    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String REQUEST_ID_ATTRIBUTE = "requestId";

    // JWT claims (see auth-service JwtConstants: sub = userId, role = ADMIN|USER)
    public static final String ROLE_CLAIM = "role";
    public static final String ADMIN_ROLE = "ADMIN";
}
