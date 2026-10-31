package com.blog.moderation.exception;

public class ReportHasActionsException extends RuntimeException {

    public ReportHasActionsException() {
        super("Report has a moderation audit history and cannot be deleted");
    }
}
