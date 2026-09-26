package com.bl2026.auth;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * Turns a Bearer token into a {@link FirebaseAuthenticationToken}. Shared by the HTTP
 * filter and the STOMP CONNECT interceptor so mock mode and verification stay identical.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FirebaseBearerAuthenticator {

    private static final String BEARER_PREFIX = "Bearer ";

    private final FirebaseTokenVerifier tokenVerifier;
    private final FirebaseProperties properties;

    public record Outcome(FirebaseAuthenticationToken authentication, Failure failure) {
        public static Outcome ok(FirebaseAuthenticationToken authentication) {
            return new Outcome(authentication, null);
        }

        public static Outcome fail(Failure failure) {
            return new Outcome(null, failure);
        }

        public boolean isOk() {
            return authentication != null;
        }
    }

    public enum Failure {
        MISSING,
        FIREBASE_DISABLED,
        INVALID
    }

    /** Accepts the raw {@code Authorization} header value, including the {@code Bearer } prefix. */
    public Outcome authenticateHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return Outcome.fail(Failure.MISSING);
        }
        return authenticateToken(authorizationHeader.substring(BEARER_PREFIX.length()).trim());
    }

    public Outcome authenticateToken(String idToken) {
        if (properties.getMock().isEnabled()
                && idToken.equals(properties.getMock().getToken())) {
            FirebaseProperties.Mock mock = properties.getMock();
            return Outcome.ok(new FirebaseAuthenticationToken(
                    mock.getFirebaseUid(), mock.getEmail(), mock.getDisplayName()));
        }

        if (!tokenVerifier.isEnabled()) {
            return Outcome.fail(Failure.FIREBASE_DISABLED);
        }

        try {
            FirebaseToken token = tokenVerifier.verify(idToken);
            return Outcome.ok(new FirebaseAuthenticationToken(token));
        } catch (FirebaseAuthException ex) {
            log.debug("Rejected Firebase ID token: {}", ex.getMessage());
            return Outcome.fail(Failure.INVALID);
        }
    }

    public static String authorizationHeaderName() {
        return HttpHeaders.AUTHORIZATION;
    }
}
