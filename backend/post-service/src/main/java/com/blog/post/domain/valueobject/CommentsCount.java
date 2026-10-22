package com.blog.post.domain.valueobject;

import com.blog.post.exception.InvalidValueException;

public final class CommentsCount {

    private long value;

    // Required by JPA
    protected CommentsCount() {
    }

    public CommentsCount(Long value) {
        if (value == null || value < 0) {
            throw new InvalidValueException("Comments count cannot be null or negative");
        }
        this.value = value;
    }

    public long value() {
        return value;
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CommentsCount that))
            return false;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return (int) (value ^ (value >>> 32));
    }

    public void increment() {
        value++;
    }

    public void decrement() {
        if (value > 0) {
            value--;
        }
    }
}
