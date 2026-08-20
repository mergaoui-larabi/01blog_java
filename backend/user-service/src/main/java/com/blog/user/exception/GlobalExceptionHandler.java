package com.blog.user.exception;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.blog.user.application.dto.error.UserErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

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

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<UserErrorResponse> handleValidation(
                        MethodArgumentNotValidException exception,
                        HttpServletRequest request) {

                System.out.println("ex" + MethodArgumentNotValidException.class);
                String message = exception.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                                .collect(Collectors.joining(", "));

                UserErrorResponse response = new UserErrorResponse(
                                Instant.now(),
                                HttpStatus.BAD_REQUEST.name(),
                                HttpStatus.BAD_REQUEST.value(),
                                message,
                                request.getRequestURI());

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(response);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<UserErrorResponse> handleMessageNotReadable(
                        HttpMessageNotReadableException exception,
                        HttpServletRequest request) {

                UserErrorResponse response = new UserErrorResponse(
                                Instant.now(),
                                HttpStatus.BAD_REQUEST.name(),
                                HttpStatus.BAD_REQUEST.value(),
                                "Invalid value for gender",
                                request.getRequestURI());

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(response);
        }
}
