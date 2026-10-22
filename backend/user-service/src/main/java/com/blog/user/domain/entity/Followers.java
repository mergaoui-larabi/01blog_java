package com.blog.user.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "followers")
public class Followers {

    @EmbeddedId
    private FollowerKey id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Followers() {
        // Required by JPA
    }

    public Followers(UUID followerId, UUID followedId) {
        if (Objects.equals(followerId, followedId)) {
            throw new IllegalArgumentException(
                    "A user cannot follow themselves"
            );
        }

        this.id = new FollowerKey(followerId, followedId);

        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public FollowerKey getId() {
        return id;
    }

    public UUID getFollowerId() {
        return id != null ? id.getFollowerId() : null;
    }

    public UUID getFollowedId() {
        return id != null ? id.getFollowedId() : null;
    }

    public UUID getFollowerKey() {
        return getFollowerId();
    }

    public UUID getFollowedKey() {
        return getFollowedId();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }
}
