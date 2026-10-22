package com.blog.post.repository;

import com.blog.post.domain.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    Optional<Post> findByIdAndAuthorId(UUID id, UUID authorId);

    @Query("""
            SELECT p FROM Post p
            WHERE p.authorId IN (:authorIds)
              AND p.visible = true
            ORDER BY p.createdAt DESC, p.id DESC
            """)
    List<Post> findFeedFirstPage(
            @Param("authorIds") Collection<UUID> authorIds,
            Pageable pageable);

    @Query("""
            SELECT p FROM Post p
            WHERE p.authorId IN (:authorIds)
              AND p.visible = true
              AND (p.createdAt < :cursorCreatedAt OR (p.createdAt = :cursorCreatedAt AND p.id < :cursorId))
            ORDER BY p.createdAt DESC, p.id DESC
            """)
    List<Post> findFeedWithCursor(
            @Param("authorIds") Collection<UUID> authorIds,
            @Param("cursorId") UUID cursorId,
            @Param("cursorCreatedAt") Instant cursorCreatedAt,
            Pageable pageable);

    @Query("""
            SELECT p FROM Post p
            WHERE p.authorId = :authorId
            ORDER BY p.createdAt DESC, p.id DESC
            """)
    List<Post> findMyPostsFirstPage(
            @Param("authorId") UUID authorId,
            Pageable pageable);

    @Query("""
            SELECT p FROM Post p
            WHERE p.authorId = :authorId
              AND (p.createdAt < :cursorCreatedAt OR (p.createdAt = :cursorCreatedAt AND p.id < :cursorId))
            ORDER BY p.createdAt DESC, p.id DESC
            """)
    List<Post> findMyPostsWithCursor(
            @Param("authorId") UUID authorId,
            @Param("cursorId") UUID cursorId,
            @Param("cursorCreatedAt") Instant cursorCreatedAt,
            Pageable pageable);
}
