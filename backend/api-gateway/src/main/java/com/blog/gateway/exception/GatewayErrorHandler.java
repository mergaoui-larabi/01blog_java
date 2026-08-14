package com.blog.gateway.exception;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ServerWebExchange;

import java.net.ConnectException;
import com.blog.gateway.constant.SecurityConstants;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class GatewayErrorHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GatewayErrorHandler.class);
    private ObjectMapper objectMapper;

    public GatewayErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable throwable) {
        HttpStatus status;
        GatewayErrorCode code;
        String message;

        switch (throwable) {

            case GatewayException exception -> {
                status = exception.getStatus();
                code = exception.getCode();
                message = exception.getMessage();
            }

            case NoResourceFoundException exception -> {
                status = HttpStatus.NOT_FOUND;
                code = GatewayErrorCode.ROUTE_NOT_FOUND;
                message = "No route found for the requested resource";
            }

            case Throwable exception when isConnectionRefused(exception) == true -> {
                status = HttpStatus.SERVICE_UNAVAILABLE;
                code = GatewayErrorCode.SERVICE_UNAVAILABLE;
                message = "The requested service is currently unavailable";
            }

            default -> {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                code = GatewayErrorCode.INTERNAL_SERVER_ERROR;
                message = "An unexpected error occurred";

                log.error(
                        "Unhandled Gateway exception",
                        throwable);
            }
        }

        String requestId = exchange.getAttribute(
                SecurityConstants.REQUEST_ID_ATTRIBUTE);

        GatewayErrorResponse response = new GatewayErrorResponse(
                Instant.now(),
                status.value(),
                code,
                message,
                exchange.getRequest().getPath().value(),
                requestId);

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse()
                .getHeaders()
                .setContentType(MediaType.APPLICATION_JSON);

        try {
            byte[] body = objectMapper.writeValueAsBytes(response);
            return exchange.getResponse().writeWith(
                    Mono.just(exchange.getResponse().bufferFactory().wrap(body)));
        } catch (Exception exception) {
            return Mono.error(exception);
        }
    }

    private boolean isConnectionRefused(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof ConnectException exception) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }
}
