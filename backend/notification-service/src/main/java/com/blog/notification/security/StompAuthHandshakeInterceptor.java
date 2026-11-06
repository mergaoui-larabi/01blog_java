package com.blog.notification.security;

import java.util.Map;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * A browser websocket handshake cannot send an Authorization header, so the STOMP endpoint takes the
 * access token as an `access_token` query parameter and this interceptor validates it with the same
 * decoder the REST API uses.
 */
public class StompAuthHandshakeInterceptor implements HandshakeInterceptor {

    public static final String PRINCIPAL_ATTRIBUTE = "blog.stomp.principal";

    private static final String TOKEN_PARAMETER = "access_token";

    private final JwtDecoder jwtDecoder;

    public StompAuthHandshakeInterceptor(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {

        MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUri(request.getURI())
                .build()
                .getQueryParams();
        String token = queryParams.getFirst(TOKEN_PARAMETER);

        if (token == null || token.isBlank()) {
            response.setStatusCode(HttpStatusCode.valueOf(401));
            return false;
        }

        try {
            Jwt jwt = jwtDecoder.decode(token);
            attributes.put(PRINCIPAL_ATTRIBUTE, new UsernamePasswordAuthenticationToken(
                    jwt.getSubject(),
                    null,
                    AuthorityUtils.NO_AUTHORITIES));
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            response.setStatusCode(HttpStatusCode.valueOf(401));
            return false;
        }
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
        // Nothing to clean up.
    }
}
