
package com.blog.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.blog.user.application.dto.request.AuthenticateUserRequest;
import com.blog.user.application.dto.request.CreateUserRequest;
import com.blog.user.application.dto.request.UpdateUserAvatarRequest;
import com.blog.user.application.dto.response.UserAuthenticationResponse;
import com.blog.user.application.dto.response.UserResponse;
import com.blog.user.domain.entity.User;
import com.blog.user.domain.model.UserRole;
import com.blog.user.domain.valueobject.Age;
import com.blog.user.domain.valueobject.Email;
import com.blog.user.domain.valueobject.Gender;
import com.blog.user.domain.valueobject.Password;
import com.blog.user.domain.valueobject.Username;
import com.blog.user.exception.EmailAlreadyExistsException;
import com.blog.user.exception.InvalidCredentialsException;
import com.blog.user.exception.UsernameAlreadyExistsException;
import com.blog.user.repository.UserRepository;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User getUserById(UUID id) {
        return userRepository.findById(id)
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
    public User updateAvatarMediaId(UpdateUserAvatarRequest request) {
        System.out.println("OwnerId: " + request.ownerId() + "avatar media id" + request.avatarMediaId());
        User user = userRepository.findById(request.ownerId())
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));
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

        return new UserAuthenticationResponse(user.getId(), user.getRole());
    }
}