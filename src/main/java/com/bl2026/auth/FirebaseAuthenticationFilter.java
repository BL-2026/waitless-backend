package com.bl2026.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    private final FirebaseBearerAuthenticator bearerAuthenticator;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return PublicEndpoints.isPublic(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        FirebaseBearerAuthenticator.Outcome outcome = bearerAuthenticator.authenticateHeader(header);

        if (outcome.isOk()) {
            SecurityContextHolder.getContext().setAuthentication(outcome.authentication());
            chain.doFilter(request, response);
            return;
        }

        if (outcome.failure() == FirebaseBearerAuthenticator.Failure.MISSING) {
            // No credentials presented. Leave the context empty and let the entry point answer 401.
            chain.doFilter(request, response);
            return;
        }

        SecurityContextHolder.clearContext();
        if (outcome.failure() == FirebaseBearerAuthenticator.Failure.FIREBASE_DISABLED) {
            writeError(response, HttpStatus.SERVICE_UNAVAILABLE,
                    "Firebase authentication is not configured on this server");
            return;
        }

        writeError(response, HttpStatus.UNAUTHORIZED, "Invalid or expired Firebase ID token");
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"status\":%d,\"error\":\"%s\",\"message\":\"%s\"}"
                .formatted(status.value(), status.getReasonPhrase(), message));
    }
}
