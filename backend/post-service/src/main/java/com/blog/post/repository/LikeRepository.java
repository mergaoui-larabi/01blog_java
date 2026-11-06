package com.blog.post.repository;

import com.blog.post.domain.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface LikeRepository extends JpaRepository<Like, UUID> {

    Optional<Like> findByPostIdAndUserId(UUID postId, UUID userId);

    List<Like> findByUserId(UUID userId);

    boolean existsByPostIdAndUserId(UUID postId, UUID userId);

    long countByPostId(UUID postId);

    void deleteByPostIdAndUserId(UUID postId, UUID userId);

    @Query("SELECT l.postId FROM Like l WHERE l.userId = :userId AND l.postId IN (:postIds)")
    Set<UUID> findLikedPostIdsByUserIdAndPostIdIn(
            @Param("userId") UUID userId,
            @Param("postIds") Collection<UUID> postIds);
}
