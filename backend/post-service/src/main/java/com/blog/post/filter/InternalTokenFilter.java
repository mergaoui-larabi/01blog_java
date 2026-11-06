package com.blog.post.filter;

import com.blog.post.constant.SecurityConstants;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/**
 * Authenticates another microservice on /internal/** endpoints. Runs before the bearer token filter
 * so that internal calls, which carry no user JWT, are still granted INTERNAL_SERVICE.
 */
public class InternalTokenFilter extends OncePerRequestFilter {

    private final String expectedToken;

    public InternalTokenFilter(String expectedToken) {
        this.expectedToken = expectedToken;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String presented = request.getHeader(SecurityConstants.INTERNAL_TOKEN_HEADER);

        // MessageDigest.isEqual keeps the comparison constant time, unlike String.equals.
        if (!expectedToken.isEmpty() && presented != null && MessageDigest.isEqual(
                expectedToken.getBytes(StandardCharsets.UTF_8),
                presented.getBytes(StandardCharsets.UTF_8))) {
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    SecurityConstants.INTERNAL_SERVICE_PRINCIPAL,
                    null,
                    List.of(new SimpleGrantedAuthority(SecurityConstants.INTERNAL_SERVICE_AUTHORITY))));
        }

        filterChain.doFilter(request, response);
    }
}
