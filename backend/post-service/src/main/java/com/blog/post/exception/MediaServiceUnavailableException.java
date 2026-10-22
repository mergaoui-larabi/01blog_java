package com.blog.post.exception;

public class MediaServiceUnavailableException extends RuntimeException {
    public MediaServiceUnavailableException(String message) {
        super(message);
    }

    public MediaServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
