package com.blog.notification.security;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

/**
 * Exposes the user id validated during the handshake as the session Principal, which is what makes
 * /user/queue/notifications resolve to a single follower.
 */
public class StompHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(
            ServerHttpRequest request,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {

        Object principal = attributes.get(StompAuthHandshakeInterceptor.PRINCIPAL_ATTRIBUTE);
        return principal instanceof Authentication authentication ? authentication : null;
    }
}
