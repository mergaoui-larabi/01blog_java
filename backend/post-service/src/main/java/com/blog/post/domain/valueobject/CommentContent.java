package com.blog.post.domain.valueobject;

import com.blog.post.exception.InvalidValueException;
import jakarta.persistence.Embeddable;

@Embeddable
public final class CommentContent {

    private String value;

    protected CommentContent() {
    }

    public CommentContent(String value) {
        if (value == null) {
            throw new InvalidValueException("Comment content cannot be null");
        }
        if (value.isBlank()) {
            throw new InvalidValueException("Comment content cannot be blank");
        }
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
