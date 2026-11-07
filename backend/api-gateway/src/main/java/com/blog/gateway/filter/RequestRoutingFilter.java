package com.blog.gateway.filter;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import com.blog.gateway.exception.GatewayErrorCode;
import com.blog.gateway.exception.GatewayException;

import reactor.core.publisher.Mono;

@Component
public class RequestRoutingFilter implements WebFilter, Ordered {

    private final Map<String, Boolean> ALLOWED_METHODS = Map.of(
            "GET", true,
            "POST", true,
            "PUT", true,
            "PATCH", true,
            "DELETE", true,
            "OPTIONS", true);

    private final Map<PathPattern, Set<HttpMethod>> SERVER_ROUTES = Map.ofEntries(
            Map.entry(PathPatternParser.defaultInstance.parse("/actuator/**"),
                    Set.of(HttpMethod.GET)),
            // user-service routes internal
            Map.entry(PathPatternParser.defaultInstance.parse("/internal/users/{userId}/following"),
                    Set.of(HttpMethod.GET)),
            Map.entry(PathPatternParser.defaultInstance.parse("/internal/users/{userId}/followers"),
                    Set.of(HttpMethod.GET)),
            Map.entry(PathPatternParser.defaultInstance.parse("/internal/users/{userId}/role"),
                    Set.of(HttpMethod.GET)),
            Map.entry(PathPatternParser.defaultInstance.parse("/internal/users/authenticate"),
                    Set.of(HttpMethod.POST)),

            // user-service routes api
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/users/ping"),
                    Set.of(HttpMethod.GET)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/users/me"),
                    Set.of(HttpMethod.GET)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/users"),
                    Set.of(HttpMethod.GET)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/users/id/{userId}"),
                    Set.of(HttpMethod.GET)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/users/{username}"),
                    Set.of(HttpMethod.GET)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/users/{userId}/follow"),
                    Set.of(HttpMethod.POST, HttpMethod.DELETE)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/users/signup"),
                    Set.of(HttpMethod.POST)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/users/update"),
                    Set.of(HttpMethod.PATCH)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/users/{userId}/ban"),
                    Set.of(HttpMethod.PATCH)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/users/{userId}/unban"),
                    Set.of(HttpMethod.PATCH)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/users/{userId}"),
                    Set.of(HttpMethod.DELETE)),

            // auth-service routes internal
            Map.entry(PathPatternParser.defaultInstance.parse("/.well-known/jwks.json"),
                    Set.of(HttpMethod.GET)),

            // auth-service routes api
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/auth/ping"),
                    Set.of(HttpMethod.GET)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/auth/signin"),
                    Set.of(HttpMethod.POST)),

            // media-service routes api
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/medias/{mediaId}/avatar"),
                    Set.of(HttpMethod.GET)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/medias/{mediaId}/content"),
                    Set.of(HttpMethod.GET)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/medias/ping"),
                    Set.of(HttpMethod.GET)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/medias/avatars"),
                    Set.of(HttpMethod.POST)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/medias/posts"),
                    Set.of(HttpMethod.POST)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/medias/{mediaId}"),
                    Set.of(HttpMethod.PUT, HttpMethod.DELETE)),

            // moderation-service routes internal
            Map.entry(PathPatternParser.defaultInstance.parse("/internal/users/{userId}/cleanup"),
                    Set.of(HttpMethod.POST)),

            // moderation-service routes api
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/reports/ping"),
                    Set.of(HttpMethod.GET)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/reports"),
                    Set.of(HttpMethod.GET)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/reports/posts"),
                    Set.of(HttpMethod.POST)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/reports/users"),
                    Set.of(HttpMethod.POST)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/reports/{reportId}"),
                    Set.of(HttpMethod.PATCH, HttpMethod.DELETE)),

            // notification-service routes internal
            Map.entry(PathPatternParser.defaultInstance.parse("/internal/notifications"),
                    Set.of(HttpMethod.POST)),
            Map.entry(PathPatternParser.defaultInstance.parse("/internal/notifications/post-created"),
                    Set.of(HttpMethod.POST)),

            // notification-service routes api
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/notifications/ping"),
                    Set.of(HttpMethod.GET)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/notifications"),
                    Set.of(HttpMethod.GET)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/notifications/unread-count"),
                    Set.of(HttpMethod.GET)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/notifications/read-all"),
                    Set.of(HttpMethod.PATCH)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/notifications/{notificationId}"),
                    Set.of(HttpMethod.PATCH, HttpMethod.DELETE)),

            // post-service routes internal
            // POST /internal/users/{userId}/cleanup is already declared above.
            // post-service routes api
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/posts/ping"),
                    Set.of(HttpMethod.GET)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/posts/admin/search"),
                    Set.of(HttpMethod.GET)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/posts/admin/ping"),
                    Set.of(HttpMethod.GET)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/posts/admin/{postId}"),
                    Set.of(HttpMethod.DELETE)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/posts/admin/{postId}/hidden"),
                    Set.of(HttpMethod.PATCH)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/posts/feed"),
                    Set.of(HttpMethod.GET)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/posts/me"),
                    Set.of(HttpMethod.GET)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/posts"),
                    Set.of(HttpMethod.POST)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/posts/{postId}"),
                    Set.of(HttpMethod.GET, HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.DELETE)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/posts/{postId}/likes"),
                    Set.of(HttpMethod.POST, HttpMethod.DELETE)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/posts/{postId}/comments"),
                    Set.of(HttpMethod.GET, HttpMethod.POST)),
            Map.entry(PathPatternParser.defaultInstance.parse("/api/v1/posts/{postId}/comments/{commentId}"),
                    Set.of(HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.DELETE)));

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 2;

    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        // get the method and routing path
        HttpMethod method = exchange.getRequest().getMethod();

        // Let CORS handle preflight requests.
        if (method == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }
        // 406
        if (!ALLOWED_METHODS.get(method.name())) {
            throw new GatewayException(GatewayErrorCode.NOT_ACCEPTABLE, HttpStatus.NOT_ACCEPTABLE,
                    "this method is not supported by the server");
        }

        PathContainer path = exchange.getRequest()
                .getPath()
                .pathWithinApplication();

        Set<HttpMethod> allowedMethods = SERVER_ROUTES.entrySet()
                .stream()
                .filter(entry -> entry.getKey().matches(path))
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toSet());
        // 404
        if (allowedMethods.isEmpty()) {
            throw new GatewayException(GatewayErrorCode.ROUTE_NOT_FOUND, HttpStatus.NOT_FOUND,
                    "the requested route does not exist");
        }
        // 405
        if (method == null || !allowedMethods.contains(method)) {
            throw new GatewayException(GatewayErrorCode.METHOD_NOT_ALLOWED, HttpStatus.METHOD_NOT_ALLOWED,
                    "the requested method is not allowed for this route");
        }

        return chain.filter(exchange);
    }
}
