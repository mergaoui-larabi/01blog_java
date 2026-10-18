package com.blog.post.domain.valueobject;

import com.blog.post.exception.InvalidValueException;
import jakarta.persistence.Embeddable;

@Embeddable
public final class CommentsCount {

    private Long value;

    protected CommentsCount() {
    }

    public CommentsCount(Long value) {
        if (value == null) {
            throw new InvalidValueException("Comments count cannot be null");
        }
        if (value < 0) {
            throw new InvalidValueException("Comments count cannot be negative");
        }
        this.value = value;
    }

    public Long value() {
        return value;
    }
}
