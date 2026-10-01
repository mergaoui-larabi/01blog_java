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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import java.net.ConnectException;
import java.net.UnknownHostException;
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

            case ResponseStatusException exception -> {
                HttpStatus resolved = HttpStatus.resolve(exception.getStatusCode().value());
                status = resolved != null ? resolved : HttpStatus.INTERNAL_SERVER_ERROR;
                code = mapStatusToCode(status);
                message = exception.getReason() != null
                        ? exception.getReason()
                        : status.getReasonPhrase();
            }

            case Throwable exception when isDownstreamUnreachable(exception) == true -> {
                status = HttpStatus.SERVICE_UNAVAILABLE;
                code = GatewayErrorCode.SERVICE_UNAVAILABLE;
                message = "The requested service is currently unavailable";
            }

            case Throwable exception when isTimeout(exception) == true -> {
                status = HttpStatus.GATEWAY_TIMEOUT;
                code = GatewayErrorCode.GATEWAY_TIMEOUT;
                message = "The request timed out while waiting for the requested service";
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

    private boolean isDownstreamUnreachable(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof ConnectException
                    || current instanceof UnknownHostException) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }

    private boolean isTimeout(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            // covers java.net.SocketTimeoutException, netty Read/WriteTimeoutException, ...
            if (current instanceof java.util.concurrent.TimeoutException
                    || current.getClass().getName().contains("TimeoutException")) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }

    private GatewayErrorCode mapStatusToCode(HttpStatus status) {
        if (status == HttpStatus.NOT_FOUND) {
            return GatewayErrorCode.ROUTE_NOT_FOUND;
        }
        if (status == HttpStatus.UNAUTHORIZED) {
            return GatewayErrorCode.UNAUTHORIZED;
        }
        if (status == HttpStatus.FORBIDDEN) {
            return GatewayErrorCode.FORBIDDEN;
        }
        if (status == HttpStatus.SERVICE_UNAVAILABLE) {
            return GatewayErrorCode.SERVICE_UNAVAILABLE;
        }
        if (status.is4xxClientError()) {
            return GatewayErrorCode.BAD_REQUEST;
        }

        return GatewayErrorCode.INTERNAL_SERVER_ERROR;
    }
}
