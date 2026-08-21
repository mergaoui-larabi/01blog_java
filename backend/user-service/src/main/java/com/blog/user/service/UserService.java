
package com.blog.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.blog.user.application.dto.request.CreateUserRequest;
import com.blog.user.domain.entity.User;
import com.blog.user.domain.model.UserRole;
import com.blog.user.domain.valueobject.Age;
import com.blog.user.domain.valueobject.Email;
import com.blog.user.domain.valueobject.Gender;
import com.blog.user.domain.valueobject.Password;
import com.blog.user.domain.valueobject.Username;
import com.blog.user.exception.EmailAlreadyExistsException;
import com.blog.user.exception.UsernameAlreadyExistsException;
import com.blog.user.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
}