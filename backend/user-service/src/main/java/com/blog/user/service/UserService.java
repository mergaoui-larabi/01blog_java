package com.blog.user.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.blog.user.application.dto.request.AuthenticateUserRequest;
import com.blog.user.application.dto.request.CreateUserRequest;
import com.blog.user.application.dto.request.UpdateUserAvatarRequest;
import com.blog.user.application.dto.response.UserAuthenticationResponse;
import com.blog.user.application.dto.response.UserResponse;
import com.blog.user.application.dto.response.UserRoleResponse;
import com.blog.user.domain.entity.User;
import com.blog.user.domain.model.UserRole;
import com.blog.user.domain.valueobject.Age;
import com.blog.user.domain.valueobject.Email;
import com.blog.user.domain.valueobject.Gender;
import com.blog.user.domain.valueobject.Password;
import com.blog.user.domain.valueobject.Username;
import com.blog.user.exception.AccountNotActiveException;
import com.blog.user.exception.BannedUserNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import com.blog.user.exception.EmailAlreadyExistsException;
import com.blog.user.exception.InvalidCredentialsException;
import com.blog.user.exception.UsernameAlreadyExistsException;
import com.blog.user.repository.UserRepository;

import java.time.Instant;
import java.util.UUID;

@Service
public class UserService {

    private final UserBlacklistService userBlacklistService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserBlacklistService userBlacklistService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userBlacklistService = userBlacklistService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @SuppressWarnings("checkstyle:Indentation")
    @Transactional(readOnly = true)
    public Page<User> getUsersList(UUID cursor, Instant cursorInstant, String search, int size, Jwt jwt) {
        if (search == null || search.isBlank()) {
            search = "";
        }
        String subject = jwt.getSubject();
        UUID requesterId = UUID.fromString(subject);
        Pageable pageable = PageRequest.ofSize(size + 1);
        if (cursor == null && cursorInstant == null) {
            return userRepository.findFirstUsersByUsername(
                    search, requesterId, pageable);
        }
        Page<User> users = userRepository.findUsersByUsername(cursor, cursorInstant, search, requesterId, pageable);

        return users;
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getAge(),
                user.getGender(),
                user.getRole(),
                user.getAvatarMediaId(),
                user.getBanned(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    @Transactional
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));
    }

    @Transactional
    public User getUserByUser(Username username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));
    }

    @Transactional
    public User createUser(CreateUserRequest request) {
        Email email = new Email(request.email());
        Username username = new Username(request.username());
        Password password = new Password(request.password());
        Age age = new Age(request.age());
        Gender gender = new Gender(request.gender());

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }
        String passwordHash = passwordEncoder.encode(password.value());

        User user = new User(
                email,
                username,
                passwordHash,
                age,
                gender,
                UserRole.USER);

        return userRepository.save(user);
    }

    @Transactional
    public User createAdmin(String _email, String _username, String _password, short _age, Gender gender) {
        Email email = new Email(_email);
        Username username = new Username(_username);
        Password password = new Password(_password);
        Age age = new Age(_age);

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }
        String passwordHash = passwordEncoder.encode(password.value());

        User user = new User(
                email,
                username,
                passwordHash,
                age,
                gender,
                UserRole.ADMIN);

        return userRepository.save(user);
    }

    @Transactional
    public User updateAvatarMediaId(UpdateUserAvatarRequest request) {
        User user = userRepository.findById(request.ownerId())
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));
        if (!user.isActive()) {
            throw new AccountNotActiveException("Account is not active");
        }
        user.updateAvatarMediaId(request.avatarMediaId());

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserAuthenticationResponse authenticate(AuthenticateUserRequest request) {
        Email email = new Email(request.email());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (user.getBanned()) {
            throw new BannedUserNotFoundException("Account is Banned");
        }

        if (!user.isActive()) {
            throw new AccountNotActiveException("Account is not active");
        }

        return new UserAuthenticationResponse(user.getId(), user.getRole());
    }

    @Transactional(readOnly = true)
    public boolean isAdmin(Jwt jwt) {
        String subject = jwt.getSubject();
        return userRepository.findById(UUID.fromString(subject))
                .map(User::getRole)
                .map(role -> role.equals(UserRole.ADMIN))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean isAdmin(String userId) {
        return userRepository.findById(UUID.fromString(userId))
                .map(User::getRole)
                .map(role -> role.equals(UserRole.ADMIN))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public UserRoleResponse fetchUserRole(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BannedUserNotFoundException("User not found"));

        return new UserRoleResponse(user.getId(), user.getRole(), user.getBanned());
    }

    @Transactional
    public User banUser(UUID userId, Jwt authenticatedJwt) {
        if (!isAdmin(authenticatedJwt)) {
            throw new BannedUserNotFoundException("Only admins can ban users");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BannedUserNotFoundException("User not found"));
        user.ban();
        userBlacklistService.flagUser(userId, "BANNED");
        return userRepository.save(user);
    }

    @Transactional
    public User unbanUser(UUID userId, Jwt authenticatedJwt) {
        if (!isAdmin(authenticatedJwt)) {
            throw new BannedUserNotFoundException("Only admins can unban users");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BannedUserNotFoundException("User not found"));
        user.unban();
        userBlacklistService.unflagUser(userId);
        return userRepository.save(user);
    }

    /**
     * Short transaction: the account is persisted as DELETION_PENDING before any
     * cleanup call is
     * made, so a crash mid-orchestration leaves a retryable state behind.
     */
    @Transactional
    public User markDeletionPending(UUID userId) {
        User user = requireUser(userId);
        user.requestDeletion();
        return userRepository.save(user);
    }

    @Transactional
    public User markDeleted(UUID userId) {
        User user = requireUser(userId);
        user.markDeleted();
        userBlacklistService.flagUser(userId, "DELETED");
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public void requireActiveAccount(UUID userId) {
        if (!requireUser(userId).isActive()) {
            throw new AccountNotActiveException("Account is not active");
        }
    }

    private User requireUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));
    }
}
