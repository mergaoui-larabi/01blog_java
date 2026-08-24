package com.blog.auth.constant;

import java.util.regex.Pattern;

public final class JwtConstants {
    private JwtConstants() {
    }

    // jwt
    public static final String HEADER_TEMPLATE = """
            {
              "alg":"RS256",
              "typ":"JWT",
              "kid":"%s"
            }
            """;
    public static final String PAYLOAD_TEMPLATE = """
            {
              "sub":"%s",
              "iss":"%s",
              "iat":%d,
              "exp":%d
            }
            """;
}
