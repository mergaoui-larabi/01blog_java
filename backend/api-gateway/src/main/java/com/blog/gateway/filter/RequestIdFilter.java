package com.blog.gateway.filter;

import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import com.blog.gateway.constant.SecurityConstants;

import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class RequestIdFilter implements WebFilter, Ordered {

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            WebFilterChain chain) {

        String requestId = UUID.randomUUID().toString();

        exchange.getAttributes().put(
                SecurityConstants.REQUEST_ID_ATTRIBUTE,
                requestId);

        exchange.getResponse()
                .getHeaders()
                .set(
                        SecurityConstants.REQUEST_ID_HEADER,
                        requestId);

        return chain.filter(exchange);
    }
}
