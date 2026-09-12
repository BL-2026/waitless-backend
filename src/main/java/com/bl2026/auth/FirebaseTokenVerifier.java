package com.bl2026.auth;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Wraps the Firebase Admin SDK so the rest of the app never touches it directly.
 *
 * <p>Initialization is tolerant of a missing service account key so the app still boots for
 * local work on the public (customer-facing) endpoints. When the key is absent
 * {@link #isEnabled()} returns false and the auth filter rejects account requests with 503.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FirebaseTokenVerifier {

    private final FirebaseProperties properties;
    private final ResourceLoader resourceLoader;

    private volatile FirebaseAuth firebaseAuth;

    @PostConstruct
    void init() {
        FirebaseProperties.Credentials credentials = properties.getCredentials();
        boolean inline = StringUtils.hasText(credentials.getJson());

        if (!inline && !StringUtils.hasText(credentials.getPath())) {
            log.warn("Neither firebase.credentials.json nor firebase.credentials.path is set - "
                    + "Firebase ID token verification is DISABLED. Authenticated endpoints will "
                    + "return 503 until one of them is configured.");
            return;
        }

        String source = inline ? "firebase.credentials.json" : credentials.getPath();

        try (InputStream credentialsStream = inline
                ? new ByteArrayInputStream(decodeJson(credentials.getJson()))
                : openCredentials(credentials.getPath())) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            this.firebaseAuth = FirebaseAuth.getInstance();
            log.info("Firebase Admin SDK initialized from {}", source);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to initialize Firebase from " + source, ex);
        }
    }

    /**
     * A service account key pasted into a dashboard field is usually base64-encoded, since
     * the private key's embedded newlines rarely survive the round trip. Accept either form
     * so the variable can be set whichever way is convenient.
     */
    private byte[] decodeJson(String value) {
        String trimmed = value.trim();
        if (trimmed.startsWith("{")) {
            return trimmed.getBytes(StandardCharsets.UTF_8);
        }
        try {
            return Base64.getDecoder().decode(trimmed);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "firebase.credentials.json is neither JSON nor valid base64", ex);
        }
    }

    public boolean isEnabled() {
        return firebaseAuth != null;
    }

    /**
     * @throws FirebaseAuthException if the token is missing, expired, revoked or otherwise invalid
     */
    public FirebaseToken verify(String idToken) throws FirebaseAuthException {
        if (firebaseAuth == null) {
            throw new IllegalStateException("Firebase is not configured");
        }
        return firebaseAuth.verifyIdToken(idToken);
    }

    private InputStream openCredentials(String path) throws IOException {
        String location = path.contains(":") ? path : "file:" + path;
        return resourceLoader.getResource(location).getInputStream();
    }
}
