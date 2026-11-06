package com.blog.user.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.blog.user.domain.model.AccountStatus;
import com.blog.user.domain.entity.FollowerKey;
import com.blog.user.domain.entity.Followers;

public interface FollowersRepository extends JpaRepository<Followers, FollowerKey> {

    @Query("SELECT f.id.followedId FROM Followers f WHERE f.id.followerId = :followerId")
    List<UUID> findFollowedIdsByFollowerId(@Param("followerId") UUID followerId);

    @Query("SELECT f.id.followerId FROM Followers f WHERE f.id.followedId = :followedId")
    List<UUID> findFollowerIdsByFollowedId(@Param("followedId") UUID followedId);

    @Query("""
                    SELECT f.id.followerId FROM Followers f, User u
                    WHERE u.id = f.id.followerId
                      AND f.id.followedId = :followedId
                      AND u.status = :status
                    """)
    List<UUID> findFollowerIdsByFollowedIdWithStatus(
                    @Param("followedId") UUID followedId,
                    @Param("status") AccountStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                    DELETE FROM Followers f
                    WHERE f.id.followerId = :userId OR f.id.followedId = :userId
                    """)
    long deleteAllForUser(@Param("userId") UUID userId);

    List<Followers> findByIdFollowerId(UUID followerId);

    List<Followers> findByIdFollowedId(UUID followedId);

    default boolean existsByFollowerIdAndFollowedId(UUID followerId, UUID followedId) {
        return existsById(new FollowerKey(followerId, followedId));
    }

    long countByIdFollowerId(UUID followerId);

    long countByIdFollowedId(UUID followedId);
}
