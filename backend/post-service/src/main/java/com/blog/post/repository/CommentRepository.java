package com.blog.post.repository;

import com.blog.post.domain.entity.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    @Query("""
            select c
            from Comment c
            where c.postId = :postId
              and (:cursorTime is null or (c.createdAt > :cursorTime or (c.createdAt = :cursorTime and c.id > :cursorId)))
            order by c.createdAt asc, c.id asc
            """)
    List<Comment> findCommentsByPostIdWithCursor(
            @Param("postId") UUID postId,
            @Param("cursorId") UUID cursorId,
            @Param("cursorTime") Instant cursorTime,
            Pageable pageable);

    List<Comment> findByPostIdOrderByCreatedAtAsc(UUID postId, Pageable pageable);

    long countByPostId(UUID postId);

    void deleteByPostId(UUID postId);
}
