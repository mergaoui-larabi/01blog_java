package com.blog.notification.filter;

import com.blog.notification.constant.SecurityConstants;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        long start = System.currentTimeMillis();

        String requestId = request.getHeader(SecurityConstants.REQUEST_ID_HEADER);

        try {
            filterChain.doFilter(request, response);
        } finally {

            long duration = System.currentTimeMillis() - start;

            log.info(
                    "\nNOTIFICATION_SERVICE:\nrequestId={}\nmethod={} path={} status={} duration={}ms\n",
                    requestId,
                    request.getMethod(),
                    request.getRequestURL(),
                    response.getStatus(),
                    duration);
        }
    }
}
