
package com.blog.user.domain.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;

import java.time.Instant;
import java.util.UUID;

import com.blog.user.domain.model.UserRole;
import com.blog.user.domain.valueobject.Email;
import com.blog.user.domain.valueobject.Username;
import com.blog.user.domain.valueobject.Age;
import com.blog.user.domain.valueobject.Gender;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email", nullable = false, unique = true, length = 255))
    private Email email;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "username", nullable = false, unique = true, length = 255))
    private Username username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "age", nullable = false))
    private Age age;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "gender", nullable = false, length = 30))
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserRole role;

    @Column(name = "avatar_media_id")
    private UUID avatarMediaId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // required by jpa
    protected User() {
    }

    public User(
            Email email,
            Username username,
            String passwordHash,
            Age age,
            Gender gender,
            UserRole role) {

        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
        this.age = age;
        this.gender = gender;
        this.role = role;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // getters
    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email.value();
    }

    public String getUsername() {
        return username.value();
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Short getAge() {
        return age.value();
    }

    public String getGender() {
        return gender.toString();
    }

    public UserRole getRole() {
        return role;
    }

    public UUID getAvatarMediaId() {
        return avatarMediaId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}