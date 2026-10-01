package com.blog.auth.exception;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.blog.auth.application.dto.error.AuthErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 400
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<AuthErrorResponse> handleInvalidRequest(
            InvalidRequestException exception,
            HttpServletRequest request) {

        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<AuthErrorResponse> handleMalformedRequestBody(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {

        return buildResponse(HttpStatus.BAD_REQUEST, "Malformed request body", request);
    }

    // 401
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<AuthErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException exception,
            HttpServletRequest request) {

        return buildResponse(HttpStatus.UNAUTHORIZED, exception.getMessage(), request);
    }

    // 404
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<AuthErrorResponse> handleNoResourceFound(
            NoResourceFoundException exception,
            HttpServletRequest request) {

        return buildResponse(HttpStatus.NOT_FOUND, "Resource not found", request);
    }

    // 405
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<AuthErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request) {

        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, exception.getMessage(), request);
    }

    // 415
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<AuthErrorResponse> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException exception,
            HttpServletRequest request) {

        return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, exception.getMessage(), request);
    }

    // 503
    @ExceptionHandler(UserServiceUnavailableException.class)
    public ResponseEntity<AuthErrorResponse> handleUserServiceUnavailable(
            UserServiceUnavailableException exception,
            HttpServletRequest request) {

        log.error("User service call failed on {}", request.getRequestURI(), exception);

        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage(), request);
    }

    // 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<AuthErrorResponse> handleUnexpected(
            Exception exception,
            HttpServletRequest request) {

        log.error("Unhandled exception on {}", request.getRequestURI(), exception);

        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }

    private ResponseEntity<AuthErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request) {

        AuthErrorResponse response = new AuthErrorResponse(
                Instant.now(),
                status.name(),
                status.value(),
                message != null && !message.isBlank() ? message : status.getReasonPhrase(),
                request.getRequestURI());

        return ResponseEntity
                .status(status)
                .body(response);
    }
}
