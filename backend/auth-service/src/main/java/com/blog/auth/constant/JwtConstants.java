package com.blog.auth.constant;

public final class JwtConstants {
  private JwtConstants() {
  }

  // jwt
  public static final String HEADER_TEMPLATE = """
      {
        "alg":"RS256",
        "typ":"JWT",
        "kid":"%s"
      }""";
  public static final String PAYLOAD_TEMPLATE = """
      {
        "sub":"%s",
        "iss":"%s",
        "role":"%s",
        "iat":%d,
        "exp":%d
      }
      """;
}
