package com.blog.media.exception;

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
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.blog.media.application.dto.error.BlogErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        // 400
        @ExceptionHandler(InvalidValueException.class)
        public ResponseEntity<BlogErrorResponse> handleInvalidValue(
                        InvalidValueException exception,
                        HttpServletRequest request) {
                return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<BlogErrorResponse> handleMethodArgumentNotValid(
                        MethodArgumentNotValidException exception,
                        HttpServletRequest request) {
                String message = exception.getBindingResult().getFieldErrors().stream()
                                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                                .collect(Collectors.joining(", "));
                return buildResponse(HttpStatus.BAD_REQUEST, message, request);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<BlogErrorResponse> handleMalformedRequestBody(
                        HttpMessageNotReadableException exception,
                        HttpServletRequest request) {
                return buildResponse(HttpStatus.BAD_REQUEST, "Malformed request body", request);
        }

        // 403
        @ExceptionHandler(MediaAccessDeniedException.class)
        public ResponseEntity<BlogErrorResponse> handleMediaAccessDenied(
                        MediaAccessDeniedException exception,
                        HttpServletRequest request) {
                return buildResponse(HttpStatus.FORBIDDEN, exception.getMessage(), request);
        }

        // 404
        @ExceptionHandler(MediaNotFoundException.class)
        public ResponseEntity<BlogErrorResponse> handleMediaNotFound(
                        MediaNotFoundException exception,
                        HttpServletRequest request) {
                return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request);
        }

        @ExceptionHandler(NoResourceFoundException.class)
        public ResponseEntity<BlogErrorResponse> handleNoResourceFound(
                        NoResourceFoundException exception,
                        HttpServletRequest request) {
                return buildResponse(HttpStatus.NOT_FOUND, "Resource not found", request);
        }

        // 405
        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<BlogErrorResponse> handleMethodNotSupported(
                        HttpRequestMethodNotSupportedException exception,
                        HttpServletRequest request) {
                return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, exception.getMessage(), request);
        }

        // 413
        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public ResponseEntity<BlogErrorResponse> handleMaxUploadSize(
                        MaxUploadSizeExceededException exception,
                        HttpServletRequest request) {
                return buildResponse(HttpStatus.PAYLOAD_TOO_LARGE, "Uploaded file is too large", request);
        }

        // 415
        @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
        public ResponseEntity<BlogErrorResponse> handleMediaTypeNotSupported(
                        HttpMediaTypeNotSupportedException exception,
                        HttpServletRequest request) {
                return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, exception.getMessage(), request);
        }

        // 409
        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<BlogErrorResponse> handleDataIntegrityViolation(
                        DataIntegrityViolationException exception,
                        HttpServletRequest request) {
                log.error("Data integrity violation on {}", request.getRequestURI(), exception);
                return buildResponse(HttpStatus.CONFLICT, "The request conflicts with existing data", request);
        }

        // 500
        @ExceptionHandler(Exception.class)
        public ResponseEntity<BlogErrorResponse> handleUnexpected(
                        Exception exception,
                        HttpServletRequest request) {
                log.error("Unhandled exception on {}", request.getRequestURI(), exception);
                return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
        }

        private ResponseEntity<BlogErrorResponse> buildResponse(
                        HttpStatus status,
                        String message,
                        HttpServletRequest request) {
                BlogErrorResponse response = new BlogErrorResponse(
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
