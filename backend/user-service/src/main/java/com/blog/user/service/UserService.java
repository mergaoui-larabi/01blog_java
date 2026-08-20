
package com.blog.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.blog.user.application.dto.request.CreateUserRequest;
import com.blog.user.domain.entity.User;
import com.blog.user.domain.model.UserRole;
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
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }
        String passwordHash = passwordEncoder.encode(request.password());

        User user = new User(
                request.email(),
                request.username(),
                passwordHash,
                request.age(),
                request.gender(),

                UserRole.USER);

        return userRepository.save(user);
    }
}