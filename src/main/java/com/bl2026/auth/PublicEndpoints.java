package com.bl2026.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;

import java.util.List;

/**
 * Single source of truth for the endpoints customers hit without ever authenticating.
 * Referenced both by {@link SecurityConfig} (to permit them) and by
 * {@link FirebaseAuthenticationFilter} (to skip token verification entirely).
 */
public final class PublicEndpoints {

    public record Endpoint(HttpMethod method, String pattern) {
    }

    public static final List<Endpoint> ENDPOINTS = List.of(
            new Endpoint(HttpMethod.GET, "/api/tables/*"),
            new Endpoint(HttpMethod.POST, "/api/requests")
    );

    /** Paths that carry no account identity at all (STOMP handshake, health checks). */
    public static final List<String> UNSECURED_PATTERNS = List.of(
            "/ws/**",
            "/actuator/health",
            "/actuator/health/**"
    );

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    private PublicEndpoints() {
    }

    public static boolean isPublic(HttpServletRequest request) {
        String path = request.getRequestURI();
        for (String pattern : UNSECURED_PATTERNS) {
            if (MATCHER.match(pattern, path)) {
                return true;
            }
        }
        for (Endpoint endpoint : ENDPOINTS) {
            if (endpoint.method().matches(request.getMethod()) && MATCHER.match(endpoint.pattern(), path)) {
                return true;
            }
        }
        return false;
    }
}
