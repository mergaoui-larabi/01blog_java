package com.blog.post.domain.entity;

import com.blog.post.domain.valueobject.AuthorId;
import com.blog.post.domain.valueobject.CommentContent;
import com.blog.post.domain.valueobject.PostId;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "post_id", nullable = false))
    private PostId postId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "author_id", nullable = false))
    private AuthorId authorId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private CommentContent content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Comment() {
    }

    public Comment(PostId postId, AuthorId authorId, CommentContent content) {
        this.postId = postId;
        this.authorId = authorId;
        this.content = content;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public PostId getPostId() {
        return postId;
    }

    public AuthorId getAuthorId() {
        return authorId;
    }

    public String getContent() {
        return content.value();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void updateContent(CommentContent content) {
        this.content = content;
        updatedAt = Instant.now();
    }
}
