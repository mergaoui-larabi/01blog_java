package com.blog.post.domain.valueobject;

import com.blog.post.constant.ValidationConstants;
import com.blog.post.exception.InvalidValueException;

import jakarta.persistence.Embeddable;

@Embeddable
public final class CommentContent {

    private String value;

    // Required by JPA
    protected CommentContent() {
    }

    public CommentContent(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidValueException("Comment content cannot be null or blank");
        }
        if (value.length() > ValidationConstants.COMMENT_CONTENT_MAX_LENGTH) {
            throw new InvalidValueException(
                    "Comment content must not exceed " + ValidationConstants.COMMENT_CONTENT_MAX_LENGTH
                            + " characters");
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CommentContent that))
            return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
