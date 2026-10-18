package com.blog.post.domain.valueobject;

import com.blog.post.exception.InvalidValueException;
import jakarta.persistence.Embeddable;

@Embeddable
public final class LikesCount {

    private Long value;

    protected LikesCount() {
    }

    public LikesCount(Long value) {
        if (value == null) {
            throw new InvalidValueException("Likes count cannot be null");
        }
        if (value < 0) {
            throw new InvalidValueException("Likes count cannot be negative");
        }
        this.value = value;
    }

    public Long value() {
        return value;
    }
}
