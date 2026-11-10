package com.blog.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.blog.gateway.constant.SecurityConstants;

import reactor.core.publisher.Mono;

@Component
public class RequestLoggingFilter implements WebFilter, Ordered {

        private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

        @Override
        public int getOrder() {
                return Ordered.HIGHEST_PRECEDENCE + 1;
        }

        @Override
        public Mono<Void> filter(
                        ServerWebExchange exchange,
                        WebFilterChain chain) {

                long start = System.currentTimeMillis();

                String requestId = exchange.getAttribute(
                                SecurityConstants.REQUEST_ID_ATTRIBUTE);

                String method = exchange.getRequest()
                                .getMethod()
                                .name();

                String path = exchange.getRequest()
                                .getPath()
                                .value();

                return chain.filter(exchange)
                                .doFinally(signal -> {

                                        long duration = System.currentTimeMillis() - start;
                                        HttpStatusCode statusCode = exchange.getResponse().getStatusCode();
                                        Integer status = statusCode != null
                                                        ? statusCode.value()
                                                        : null;

                                        log.info(
                                                        "\nAPI_GATEWAY:\nrequestId={}\nmethod={} path={} status={} duration={}ms\n",
                                                        requestId,
                                                        method,
                                                        path,
                                                        status,
                                                        duration);
                                });
        }
}