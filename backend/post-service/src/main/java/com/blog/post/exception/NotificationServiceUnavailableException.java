package com.blog.post.exception;

public class NotificationServiceUnavailableException extends RuntimeException {

    public NotificationServiceUnavailableException(String message) {
        super(message);
    }

    public NotificationServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
