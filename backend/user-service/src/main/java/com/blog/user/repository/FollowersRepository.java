package com.blog.user.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.blog.user.domain.entity.FollowerKey;
import com.blog.user.domain.entity.Followers;

public interface FollowersRepository extends JpaRepository<Followers, FollowerKey> {

    @Query("SELECT f.id.followedId FROM Followers f WHERE f.id.followerId = :followerId")
    List<UUID> findFollowedIdsByFollowerId(@Param("followerId") UUID followerId);

    @Query("SELECT f.id.followerId FROM Followers f WHERE f.id.followedId = :followedId")
    List<UUID> findFollowerIdsByFollowedId(@Param("followedId") UUID followedId);

    List<Followers> findByIdFollowerId(UUID followerId);

    List<Followers> findByIdFollowedId(UUID followedId);

    default boolean existsByFollowerIdAndFollowedId(UUID followerId, UUID followedId) {
        return existsById(new FollowerKey(followerId, followedId));
    }

    long countByIdFollowerId(UUID followerId);

    long countByIdFollowedId(UUID followedId);
}
