package com.blog.user.domain.valueobject;

import com.blog.user.exception.InvalidValueException;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public final class Gender {

    public enum Type {
        MALE,
        FEMALE
    }

    @Enumerated(EnumType.STRING)
    private Type value;

    // Required by JPA
    protected Gender() {
    }

    public Gender(String value) {
        if (value == null) {
            throw new InvalidValueException("Gender cannot be null");
        }
        value = value.trim();
        if (value.isBlank()) {
            throw new InvalidValueException("Gender cannot be blank");
        }
        try {
            this.value = Type.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new InvalidValueException("Invalid gender: " + value);
        }
    }

    public Type value() {
        return value;
    }

    @Override
    public String toString() {
        return value.name();
    }
}
