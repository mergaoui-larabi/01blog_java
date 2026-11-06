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
import java.util.Objects;
import java.util.UUID;

import com.blog.user.domain.model.AccountStatus;
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

    @Column(name = "banned", nullable = false)
    private Boolean banned = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AccountStatus status = AccountStatus.ACTIVE;

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
        this.banned = false;
        this.status = AccountStatus.ACTIVE;
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

    public Boolean getBanned() {
        return banned;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // update
    public void updateAvatarMediaId(UUID avatarMediaId) {
        this.avatarMediaId = avatarMediaId;
    }

    public void ban() {
        if (banned) {
            throw new IllegalStateException("User is already banned");
        }
        this.banned = true;
        this.updatedAt = Instant.now();
    }

    public void unban() {
        if (!banned) {
            throw new IllegalStateException("User is not banned");
        }
        this.banned = false;
        this.updatedAt = Instant.now();
    }

    /**
     * Repeated requests are allowed: DELETION_PENDING is the retryable state where cleanup resumes.
     */
    public void requestDeletion() {
        if (status == AccountStatus.DELETED) {
            throw new IllegalStateException("User account is already deleted");
        }
        this.status = AccountStatus.DELETION_PENDING;
        this.updatedAt = Instant.now();
    }

    public void markDeleted() {
        this.status = AccountStatus.DELETED;
        this.updatedAt = Instant.now();
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    void setId(UUID id) {
        this.id = id;
    }

    void setBanned(boolean banned) {
        this.banned = banned;
    }

    // test helpers
    UUID createTestId() {
        return id = UUID.randomUUID();
    }

    void createTestBanned(boolean banned) {
        this.banned = banned;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", banned=" + banned +
                '}';
    }
}
