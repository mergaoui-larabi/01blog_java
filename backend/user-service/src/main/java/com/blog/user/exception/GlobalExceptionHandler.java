package com.blog.user.exception;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.blog.user.application.dto.error.UserErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
        // 409
        @ExceptionHandler(EmailAlreadyExistsException.class)
        public ResponseEntity<UserErrorResponse> handleEmailAlreadyExists(
                        EmailAlreadyExistsException exception,
                        HttpServletRequest request) {

                UserErrorResponse response = new UserErrorResponse(
                                Instant.now(),
                                HttpStatus.CONFLICT.name(),
                                HttpStatus.CONFLICT.value(),
                                exception.getMessage(),
                                request.getRequestURI());

                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(response);
        }

        @ExceptionHandler(UsernameAlreadyExistsException.class)
        public ResponseEntity<UserErrorResponse> handleEmailAlreadyExists(
                        UsernameAlreadyExistsException exception,
                        HttpServletRequest request) {

                UserErrorResponse response = new UserErrorResponse(
                                Instant.now(),
                                HttpStatus.CONFLICT.name(),
                                HttpStatus.CONFLICT.value(),
                                exception.getMessage(),
                                request.getRequestURI());

                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(response);
        }

        // 404
        @ExceptionHandler(InvalidValueException.class)
        public ResponseEntity<UserErrorResponse> handleInvalidValue(
                        InvalidValueException exception,
                        HttpServletRequest request) {

                UserErrorResponse response = new UserErrorResponse(
                                Instant.now(),
                                HttpStatus.BAD_REQUEST.name(),
                                HttpStatus.BAD_REQUEST.value(),
                                exception.getMessage(),
                                request.getRequestURI());

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(response);
        }

        // 403

        // 404
        
        // 500
}
