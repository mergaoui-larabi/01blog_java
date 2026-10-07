package com.blog.media.exception;

public class MediaAccessDeniedException extends RuntimeException {
    public MediaAccessDeniedException(String message) {
        super(message);
    }
}
