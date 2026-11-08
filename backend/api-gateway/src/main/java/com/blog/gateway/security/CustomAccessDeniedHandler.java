package com.blog.gateway.security;

import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class CustomAccessDeniedHandler implements ServerAccessDeniedHandler {

    @Override
    public Mono<Void> handle(
            ServerWebExchange exchange,
            AccessDeniedException exception) {

        String code = "ACCESS_DENIED";
        String message = "You do not have permission to access this resource.";

        if (exception instanceof AccountRestrictedException restricted) {
            code = restricted.getCode();

            message = switch (code) {
                case "ACCOUNT_BANNED" -> "Your account has been suspended.";
                case "ACCOUNT_DELETED" -> "Your account has been deleted.";
                default -> message;
            };
        }

        String body = """
                {
                    "status": 403,
                    "code": "%s",
                    "message": "%s"
                }
                """.formatted(code, message);

        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse()
                        .bufferFactory()
                        .wrap(bytes)));
    }
}