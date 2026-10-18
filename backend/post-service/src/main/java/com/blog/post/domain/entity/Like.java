package com.blog.post.domain.entity;

import com.blog.post.domain.valueobject.PostId;
import com.blog.post.domain.valueobject.UserId;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "likes")
public class Like {

    @Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "user_id", nullable = false))
    private UserId userId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "post_id", nullable = false))
    private PostId postId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Like() {
    }

    public Like(UserId userId, PostId postId) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.postId = postId;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId.value();
    }

    public UUID getPostId() {
        return postId.value();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public UserId getUserIdEntity() {
        return userId;
    }

    public PostId getPostIdEntity() {
        return postId;
    }
}
