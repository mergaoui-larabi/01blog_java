package com.blog.post.domain.entity;

import com.blog.post.domain.valueobject.AuthorId;
import com.blog.post.domain.valueobject.CommentsCount;
import com.blog.post.domain.valueobject.LikesCount;
import com.blog.post.domain.valueobject.MediaId;
import com.blog.post.domain.valueobject.PostContent;
import com.blog.post.domain.valueobject.PostId;
import com.blog.post.domain.valueobject.Visibility;

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
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "post_id", nullable = false))
    private PostId postId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "author_id", nullable = false))
    private AuthorId authorId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private PostContent postContent;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "post_media_id"))
    private MediaId mediaId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "likes_count", nullable = false))
    private LikesCount likesCount;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "comments_count", nullable = false))
    private CommentsCount commentsCount;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "is_hidden", nullable = false))
    private Visibility hidden;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Post() {
    }

    public Post(
            PostId postId,
            AuthorId authorId,
            PostContent postContent,
            MediaId mediaId,
            LikesCount likesCount,
            CommentsCount commentsCount,
            Visibility hidden) {
        this.postId = postId;
        this.authorId = authorId;
        this.postContent = postContent;
        this.mediaId = mediaId;
        this.likesCount = likesCount;
        this.commentsCount = commentsCount;
        this.hidden = hidden;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public PostId getPostId() {
        return postId;
    }

    public AuthorId getAuthorId() {
        return authorId;
    }

    public String getContent() {
        return postContent.value();
    }

    public MediaId getMediaId() {
        return mediaId;
    }

    public Long getLikesCount() {
        return likesCount.value();
    }

    public Long getCommentsCount() {
        return commentsCount.value();
    }

    public Boolean getHidden() {
        return hidden.value();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void incrementLikesCount() {
        likesCount = new LikesCount(likesCount.value() + 1);
        updatedAt = Instant.now();
    }

    public void decrementLikesCount() {
        if (likesCount.value() > 0) {
            likesCount = new LikesCount(likesCount.value() - 1);
        }
        updatedAt = Instant.now();
    }

    public void incrementCommentsCount() {
        commentsCount = new CommentsCount(commentsCount.value() + 1);
        updatedAt = Instant.now();
    }

    public void decrementCommentsCount() {
        if (commentsCount.value() > 0) {
            commentsCount = new CommentsCount(commentsCount.value() - 1);
        }
        updatedAt = Instant.now();
    }

    public void hide() {
        this.hidden = new Visibility(true);
        updatedAt = Instant.now();
    }

    public void unhide() {
        this.hidden = new Visibility(false);
        updatedAt = Instant.now();
    }

    public void updateContent(PostContent postContent) {
        this.postContent = postContent;
        updatedAt = Instant.now();
    }

    public void updateMediaId(MediaId mediaId) {
        this.mediaId = mediaId;
        updatedAt = Instant.now();
    }

    public void updateAuthorId(AuthorId authorId) {
        this.authorId = authorId;
        updatedAt = Instant.now();
    }
}
