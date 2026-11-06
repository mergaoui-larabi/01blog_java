package com.blog.post.domain.entity;

import com.blog.post.domain.valueobject.CommentContent;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "post_id", nullable = false)
    private UUID postId;

    // Nullable: cleared when the author's account is deleted, the comment itself is kept and anonymized
    @Column(name = "author_id")
    private UUID authorId;

    @Column(name = "author_anonymized", nullable = false)
    private boolean authorAnonymized = false;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "content", nullable = false, columnDefinition = "TEXT"))
    private CommentContent content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Comment() {
    }

    public Comment(UUID postId, UUID authorId, CommentContent content) {
        this.postId = postId;
        this.authorId = authorId;
        this.content = content;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getPostId() {
        return postId;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public boolean isAuthorAnonymized() {
        return authorAnonymized;
    }

    public CommentContent getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setPostId(UUID postId) {
        this.postId = postId;
    }

    public void setAuthorId(UUID authorId) {
        this.authorId = authorId;
    }

    public void anonymizeAuthor() {
        this.authorId = null;
        this.authorAnonymized = true;
        touch();
    }

    public void setContent(CommentContent content) {
        this.content = content;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }
}
