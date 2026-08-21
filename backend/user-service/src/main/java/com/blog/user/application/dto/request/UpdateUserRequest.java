package com.blog.user.application.dto.request;

import com.blog.user.domain.valueobject.Age;
import com.blog.user.domain.valueobject.Email;
import com.blog.user.domain.valueobject.Gender;
import com.blog.user.domain.valueobject.Password;
import com.blog.user.domain.valueobject.Username;

public record UpdateUserRequest(
                String email,
                String username,
                String password,
                Short age,
                String gender) {
}