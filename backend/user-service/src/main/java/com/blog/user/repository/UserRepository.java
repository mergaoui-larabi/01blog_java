package com.blog.user.repository;

import com.blog.user.domain.entity.User;
import com.blog.user.domain.model.UserRole;
import com.blog.user.domain.valueobject.Email;
import com.blog.user.domain.valueobject.Username;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

  boolean existsByEmail(Email email);

  boolean existsByUsername(Username username);

  Optional<User> findByUsername(Username username);

  Optional<User> findByEmail(Email email);

  @Query("""
      SELECT u
      FROM User u
      WHERE LOWER(u.username.value) LIKE LOWER(CONCAT('%', :username, '%'))
        AND u.id <> :requesterId
      ORDER BY u.createdAt DESC, u.id DESC
      """)
  Page<User> findFirstUsersByUsername(
      @Param("username") String username,
      @Param("requesterId") UUID requesterId,
      Pageable pageable);

  @Query("""
      SELECT u
      FROM User u
      WHERE LOWER(u.username.value) LIKE LOWER(CONCAT('%', :username, '%'))
        AND u.id <> :requesterId
        AND (
            :cursorCreatedAt IS NULL
            OR u.createdAt < :cursorCreatedAt
            OR (u.createdAt = :cursorCreatedAt AND u.id < :cursorId)
        )
      ORDER BY u.createdAt DESC, u.id DESC
      """)
  Page<User> findUsersByUsername(
      @Param("cursorId") UUID cursorId,
      @Param("cursorCreatedAt") Instant cursorCreatedAt,
      @Param("username") String username,
      @Param("requesterId") UUID requesterId,
      Pageable pageable);

  Optional<User> findByIdAndRole(@Param("id") UUID id, @Param("role") UserRole role);
}
