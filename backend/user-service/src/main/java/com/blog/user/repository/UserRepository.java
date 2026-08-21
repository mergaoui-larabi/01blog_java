package com.blog.user.repository;

import com.blog.user.domain.entity.User;
import com.blog.user.domain.valueobject.Email;
import com.blog.user.domain.valueobject.Username;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(Email email);

    boolean existsByUsername(Username username);

}
