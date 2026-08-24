package com.blog.auth.application.client;

import com.blog.auth.application.dto.user.AuthenticateUserRequest;
import com.blog.auth.application.dto.user.UserAuthenticationResponse;

public interface UserServiceClient {

    
    UserAuthenticationResponse authenticate(
            AuthenticateUserRequest request);
}