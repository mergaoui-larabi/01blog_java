package com.blog.user.exception;

public class ServiceCleanupUnavailableException extends RuntimeException {

    public ServiceCleanupUnavailableException(String serviceName, Throwable cause) {
        super("Cleanup failed in " + serviceName, cause);
    }
}
