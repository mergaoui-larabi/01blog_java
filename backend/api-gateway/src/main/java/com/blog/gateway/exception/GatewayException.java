package com.blog.gateway.exception;

import org.springframework.http.HttpStatus;

public class GatewayException extends RuntimeException {

    private final GatewayErrorCode code;
    private final HttpStatus status;

    public GatewayException(GatewayErrorCode code, HttpStatus status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public GatewayErrorCode getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
