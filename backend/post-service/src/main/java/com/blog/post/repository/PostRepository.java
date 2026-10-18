package com.blog.post.repository;

import com.blog.post.domain.entity.Post;
import com.blog.post.domain.valueobject.AuthorId;
import com.blog.post.domain.valueobject.PostId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    Optional<Post> findByPostId(PostId postId);

    List<Post> findByAuthorId(AuthorId authorId);

    @Query("""
            SELECT p FROM Post p
            WHERE p.authorId = :authorId
               OR p.authorId IN :followingIds
            ORDER BY p.createdAt DESC, p.id DESC
            """)
    List<Post> findByAuthorIdOrFollowing(
            @Param("authorId") AuthorId authorId,
            @Param("followingIds") List<AuthorId> followingIds);

    @Query("""
            SELECT p FROM Post p
            WHERE p.authorId = :authorId
               OR p.authorId IN :followingIds
            ORDER BY p.createdAt DESC, p.id DESC
            """)
    List<Post> findFeedPosts(
            @Param("authorId") AuthorId authorId,
            @Param("followingIds") List<AuthorId> followingIds);

    @Query("""
            SELECT p FROM Post p
            WHERE p.authorId = :authorId
            ORDER BY p.createdAt DESC, p.id DESC
            """)
    List<Post> findByAuthorIdOrderByCreatedAtDesc(@Param("authorId") AuthorId authorId);

    boolean existsByPostId(PostId postId);

    @Query("""
            SELECT p FROM Post p
            WHERE p.id = :id
              AND p.authorId = :authorId
            """)
    Optional<Post> findByIdAndAuthorId(@Param("id") UUID id, @Param("authorId") AuthorId authorId);

    @Query("""
            SELECT p FROM Post p
            WHERE p.postId = :postId
              AND p.authorId = :authorId
            """)
    Optional<Post> findByPostIdAndAuthorId(@Param("postId") PostId postId, @Param("authorId") AuthorId authorId);
}
