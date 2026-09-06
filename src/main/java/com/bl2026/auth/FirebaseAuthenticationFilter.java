package com.bl2026.auth;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
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

    private static final String BEARER_PREFIX = "Bearer ";

    private final FirebaseTokenVerifier tokenVerifier;
    private final FirebaseProperties properties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return PublicEndpoints.isPublic(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (properties.getMock().isEnabled()
            && header != null
            && header.equals(BEARER_PREFIX + properties.getMock().getToken())) {
            FirebaseProperties.Mock mock = properties.getMock();
            SecurityContextHolder.getContext().setAuthentication(
                new FirebaseAuthenticationToken(mock.getFirebaseUid(), mock.getEmail(), mock.getDisplayName()));
            chain.doFilter(request, response);
            return;
        }

        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            // No credentials presented. Leave the context empty and let the entry point answer 401.
            chain.doFilter(request, response);
            return;
        }

        if (!tokenVerifier.isEnabled()) {
            writeError(response, HttpStatus.SERVICE_UNAVAILABLE,
                    "Firebase authentication is not configured on this server");
            return;
        }

        String idToken = header.substring(BEARER_PREFIX.length()).trim();
        try {
            FirebaseToken token = tokenVerifier.verify(idToken);
            SecurityContextHolder.getContext().setAuthentication(new FirebaseAuthenticationToken(token));
        } catch (FirebaseAuthException ex) {
            SecurityContextHolder.clearContext();
            log.debug("Rejected Firebase ID token: {}", ex.getMessage());
            writeError(response, HttpStatus.UNAUTHORIZED, "Invalid or expired Firebase ID token");
            return;
        }

        chain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"status\":%d,\"error\":\"%s\",\"message\":\"%s\"}"
                .formatted(status.value(), status.getReasonPhrase(), message));
    }
}
