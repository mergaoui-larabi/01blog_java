package com.blog.post.repository;

import com.blog.post.domain.entity.Comment;
import com.blog.post.domain.valueobject.PostId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPostIdOrderByCreatedAtAsc(PostId postId);

    @Query("""
            SELECT c FROM Comment c
            WHERE c.postId = :postId
            ORDER BY c.createdAt ASC, c.id ASC
            """)
    List<Comment> findCommentsByPostId(@Param("postId") PostId postId);

    Optional<Comment> findByPostIdAndId(PostId postId, Long id);

    @Query("""
            SELECT c.postId FROM Comment c
            WHERE c.id = :id
            """)
    Optional<PostId> findPostIdById(@Param("id") Long id);

    long countByPostId(PostId postId);

    void deleteByPostId(PostId postId);

    void deleteByPostIdAndAuthorId(PostId postId, com.blog.post.domain.valueobject.AuthorId authorId);

    boolean existsByPostIdAndId(PostId postId, Long id);

    @org.springframework.transaction.annotation.Transactional
    @Modifying
    @Query("DELETE FROM Comment c WHERE c.postId = :postId")
    void deleteAllByPostId(@Param("postId") PostId postId);
}
