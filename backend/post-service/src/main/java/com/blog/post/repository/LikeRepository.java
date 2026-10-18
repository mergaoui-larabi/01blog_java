package com.blog.post.repository;

import com.blog.post.domain.entity.Like;
import com.blog.post.domain.valueobject.PostId;
import com.blog.post.domain.valueobject.UserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    @Query("""
            SELECT l FROM Like l
            WHERE l.postId = :postId
              AND l.userId = :userId
            """)
    Optional<Like> findByPostIdAndUserId(@Param("postId") PostId postId, @Param("userId") UserId userId);

    long countByPostId(PostId postId);

    void deleteByPostIdAndUserId(PostId postId, UserId userId);

    boolean existsByPostIdAndUserId(PostId postId, UserId userId);


}
