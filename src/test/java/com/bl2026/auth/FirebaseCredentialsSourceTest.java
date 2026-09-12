package com.bl2026.auth;

import com.google.firebase.FirebaseApp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPairGenerator;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Managed hosts have no filesystem to drop a service account key onto, so the credentials
 * can also arrive inline as an environment variable. These cover the three ways
 * {@code firebase.credentials.*} can be supplied, plus the disabled case.
 *
 * <p>The key below is generated per-run and thrown away. Firebase parses it at startup but
 * doesn't call Google, so a structurally valid key is enough to prove which branch was taken.
 */
class FirebaseCredentialsSourceTest {

    private static final String FAKE_PROJECT = "waitless-test";

    @AfterEach
    void tearDown() {
        // FirebaseApp is a JVM-wide singleton; leaving one behind changes what the next
        // test sees.
        FirebaseApp.getApps().forEach(FirebaseApp::delete);
    }

    @Test
    void isDisabledWhenNeitherPathNorJsonIsSet() {
        FirebaseTokenVerifier verifier = verifierFor(new FirebaseProperties());

        verifier.init();

        assertThat(verifier.isEnabled())
                .as("no credentials means public endpoints still work and authenticated ones 503")
                .isFalse();
    }

    @Test
    void readsRawJsonFromConfiguration() throws Exception {
        FirebaseProperties properties = new FirebaseProperties();
        properties.getCredentials().setJson(serviceAccountJson());

        FirebaseTokenVerifier verifier = verifierFor(properties);
        verifier.init();

        assertThat(verifier.isEnabled()).isTrue();
    }

    @Test
    void readsBase64EncodedJsonFromConfiguration() throws Exception {
        String encoded = Base64.getEncoder()
                .encodeToString(serviceAccountJson().getBytes(StandardCharsets.UTF_8));

        FirebaseProperties properties = new FirebaseProperties();
        properties.getCredentials().setJson(encoded);

        FirebaseTokenVerifier verifier = verifierFor(properties);
        verifier.init();

        assertThat(verifier.isEnabled())
                .as("a key pasted into a dashboard field is usually base64, since the private "
                        + "key's newlines don't survive the round trip")
                .isTrue();
    }

    @Test
    void jsonWinsOverPath() throws Exception {
        FirebaseProperties properties = new FirebaseProperties();
        properties.getCredentials().setJson(serviceAccountJson());
        properties.getCredentials().setPath("/nonexistent/service-account.json");

        FirebaseTokenVerifier verifier = verifierFor(properties);
        verifier.init();

        assertThat(verifier.isEnabled())
                .as("the unreadable path would have thrown had it been consulted")
                .isTrue();
    }

    @Test
    void failsLoudlyOnAValueThatIsNeitherJsonNorBase64() {
        FirebaseProperties properties = new FirebaseProperties();
        properties.getCredentials().setJson("not json, not base64: %%%");

        FirebaseTokenVerifier verifier = verifierFor(properties);

        assertThatThrownBy(verifier::init)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("neither JSON nor valid base64");
    }

    @Test
    void readsAFileFromPathWhenNoJsonIsGiven() throws Exception {
        Path keyFile = Files.createTempFile("waitless-service-account", ".json");
        Files.writeString(keyFile, serviceAccountJson());

        FirebaseProperties properties = new FirebaseProperties();
        properties.getCredentials().setPath(keyFile.toString());

        FirebaseTokenVerifier verifier = verifierFor(properties);
        try {
            verifier.init();
            assertThat(verifier.isEnabled()).isTrue();
        } finally {
            Files.deleteIfExists(keyFile);
        }
    }

    private FirebaseTokenVerifier verifierFor(FirebaseProperties properties) {
        return new FirebaseTokenVerifier(properties, new DefaultResourceLoader());
    }

    /** The shape the Firebase Admin SDK expects of a service account key. */
    private String serviceAccountJson() throws Exception {
        return """
                {
                  "type": "service_account",
                  "project_id": "%s",
                  "private_key_id": "throwaway",
                  "private_key": "%s",
                  "client_email": "test@%s.iam.gserviceaccount.com",
                  "client_id": "000000000000000000000",
                  "token_uri": "https://oauth2.googleapis.com/token"
                }
                """.formatted(FAKE_PROJECT, pemPrivateKey(), FAKE_PROJECT);
    }

    private String pemPrivateKey() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        String body = Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.UTF_8))
                .encodeToString(generator.generateKeyPair().getPrivate().getEncoded());

        // JSON has no multi-line strings, so the PEM's newlines are escaped — exactly how a
        // real key file stores them.
        return ("-----BEGIN PRIVATE KEY-----\n" + body + "\n-----END PRIVATE KEY-----\n")
                .replace("\n", "\\n");
    }
}
