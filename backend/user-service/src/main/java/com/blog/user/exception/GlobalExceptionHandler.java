package com.blog.user.exception;

import java.time.Instant;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.blog.user.application.dto.error.UserErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        // 400
        @ExceptionHandler(InvalidValueException.class)
        public ResponseEntity<UserErrorResponse> handleInvalidValue(
                        InvalidValueException exception,
                        HttpServletRequest request) {

                return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<UserErrorResponse> handleMethodArgumentNotValid(
                        MethodArgumentNotValidException exception,
                        HttpServletRequest request) {

                String message = exception.getBindingResult().getFieldErrors().stream()
                                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                                .collect(Collectors.joining(", "));

                return buildResponse(HttpStatus.BAD_REQUEST, message, request);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<UserErrorResponse> handleMalformedRequestBody(
                        HttpMessageNotReadableException exception,
                        HttpServletRequest request) {

                return buildResponse(HttpStatus.BAD_REQUEST, "Malformed request body", request);
        }

        // 401
        @ExceptionHandler(InvalidCredentialsException.class)
        public ResponseEntity<UserErrorResponse> handleInvalidCredentials(
                        InvalidCredentialsException exception,
                        HttpServletRequest request) {

                return buildResponse(HttpStatus.UNAUTHORIZED, exception.getMessage(), request);
        }

        // 403
        @ExceptionHandler({BannedUserNotFoundException.class, AccountNotActiveException.class})
        public ResponseEntity<UserErrorResponse> handleBannedUserNotFound(
                        BannedUserNotFoundException exception,
                        HttpServletRequest request) {

                return buildResponse(HttpStatus.FORBIDDEN, exception.getMessage(), request);
        }

        // 404
        @ExceptionHandler(NoResourceFoundException.class)
        public ResponseEntity<UserErrorResponse> handleNoResourceFound(
                        NoResourceFoundException exception,
                        HttpServletRequest request) {

                return buildResponse(HttpStatus.NOT_FOUND, "Resource not found", request);
        }

        // 405
        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<UserErrorResponse> handleMethodNotSupported(
                        HttpRequestMethodNotSupportedException exception,
                        HttpServletRequest request) {

                return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, exception.getMessage(), request);
        }

        // 415
        @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
        public ResponseEntity<UserErrorResponse> handleMediaTypeNotSupported(
                        HttpMediaTypeNotSupportedException exception,
                        HttpServletRequest request) {

                return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, exception.getMessage(), request);
        }

        // 409
        @ExceptionHandler(EmailAlreadyExistsException.class)
        public ResponseEntity<UserErrorResponse> handleEmailAlreadyExists(
                        EmailAlreadyExistsException exception,
                        HttpServletRequest request) {

                return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request);
        }

        @ExceptionHandler(UsernameAlreadyExistsException.class)
        public ResponseEntity<UserErrorResponse> handleUsernameAlreadyExists(
                        UsernameAlreadyExistsException exception,
                        HttpServletRequest request) {

                return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request);
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<UserErrorResponse> handleDataIntegrityViolation(
                        DataIntegrityViolationException exception,
                        HttpServletRequest request) {

                log.error("Data integrity violation on {}", request.getRequestURI(), exception);

                return buildResponse(HttpStatus.CONFLICT, "The request conflicts with existing data", request);
        }

        // An account that is already deleted cannot be deleted again
        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<UserErrorResponse> handleIllegalState(
                        IllegalStateException exception,
                        HttpServletRequest request) {

                return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request);
        }

        // 500
        @ExceptionHandler(Exception.class)
        public ResponseEntity<UserErrorResponse> handleUnexpected(
                Exception exception,
                HttpServletRequest request) {

                log.error("Unhandled exception on {}", request.getRequestURI(), exception);

                return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
        }

        private ResponseEntity<UserErrorResponse> buildResponse(
                        HttpStatus status,
                        String message,
                        HttpServletRequest request) {

                UserErrorResponse response = new UserErrorResponse(
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
