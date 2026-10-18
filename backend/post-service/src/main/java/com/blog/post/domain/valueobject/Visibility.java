package com.blog.post.domain.valueobject;

import com.blog.post.exception.InvalidValueException;
import jakarta.persistence.Embeddable;

@Embeddable
public final class Visibility {

    private Boolean value;

    protected Visibility() {
    }

    public Visibility(Boolean value) {
        if (value == null) {
            throw new InvalidValueException("Visibility cannot be null");
        }
        this.value = value;
    }

    public Boolean value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
