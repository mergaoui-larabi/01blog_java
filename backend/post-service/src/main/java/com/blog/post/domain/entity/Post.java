package com.blog.post.domain.entity;

import com.blog.post.domain.valueobject.CommentsCount;
import com.blog.post.domain.valueobject.LikesCount;
import com.blog.post.domain.valueobject.PostContent;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Nullable: cleared when the author's account is deleted, the post itself is kept and anonymized
    @Column(name = "author_id")
    private UUID authorId;

    @Column(name = "author_anonymized", nullable = false)
    private boolean authorAnonymized = false;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "content", nullable = false, columnDefinition = "TEXT"))
    private PostContent content;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "post_medias", joinColumns = @JoinColumn(name = "post_id"))
    @OrderColumn(name = "position")
    @Column(name = "media_id", nullable = false)
    private List<UUID> mediaIds = new ArrayList<>();

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "likes_count", nullable = false))
    private LikesCount likesCount = new LikesCount(0L);

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "comments_count", nullable = false))
    private CommentsCount commentsCount = new CommentsCount(0L);

    @Column(name = "hidden", nullable = false)
    private Boolean hidden = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Post() {
    }

    public Post(UUID authorId, PostContent content) {
        this.authorId = authorId;
        this.content = content;
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public Post(UUID authorId, PostContent content, List<UUID> mediaIds) {
        this(authorId, content);
        if (mediaIds != null) {
            this.mediaIds = new ArrayList<>(mediaIds);
        }
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

    public UUID getId() {
        return id;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public boolean isAuthorAnonymized() {
        return authorAnonymized;
    }

    public PostContent getContent() {
        return content;
    }

    public List<UUID> getMediaIds() {
        return mediaIds;
    }

    public LikesCount getLikesCount() {
        return likesCount;
    }

    public CommentsCount getCommentsCount() {
        return commentsCount;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setAuthorId(UUID authorId) {
        this.authorId = authorId;
    }

    public void anonymizeAuthor() {
        this.authorId = null;
        this.authorAnonymized = true;
        touch();
    }

    public void setContent(PostContent content) {
        this.content = content;
    }

    public void setMediaIds(List<UUID> mediaIds) {
        this.mediaIds = mediaIds != null ? new ArrayList<>(mediaIds) : new ArrayList<>();
    }

    public void setLikesCount(LikesCount likesCount) {
        this.likesCount = likesCount;
    }

    public void setCommentsCount(CommentsCount commentsCount) {
        this.commentsCount = commentsCount;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
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

    public void incrementLikesCount() {
        this.likesCount.increment();
        touch();
    }

    public void decrementLikesCount() {
        this.likesCount.decrement();
        touch();
    }

    public void incrementCommentsCount() {
        this.commentsCount.increment();
        touch();
    }

    public void decrementCommentsCount() {
        this.commentsCount.decrement();
        touch();
    }
}
