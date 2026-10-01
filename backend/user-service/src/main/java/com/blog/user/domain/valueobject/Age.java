package com.blog.user.domain.valueobject;

import com.blog.user.exception.InvalidValueException;

import jakarta.persistence.Embeddable;

import com.blog.user.constant.ValidationConstants;

@Embeddable
public class Age {

    private Short value;

    // Required by JPA
    protected Age() {
    }

    public Age(Short value) {
        if (value == null) {
            throw new InvalidValueException("Age cannot be null");
        }
        if (value > ValidationConstants.AGE_MAX_NUMBER) {
            throw new InvalidValueException("Age must be younger than "
                    + ValidationConstants.AGE_MAX_NUMBER);
        }
        if (value < ValidationConstants.AGE_MIN_NUMBER) {
            throw new InvalidValueException("Age must be older than "
                    + ValidationConstants.AGE_MAX_NUMBER);
        }

        this.value = value;
    }

    public Short value() {
        return value;
    }
}
