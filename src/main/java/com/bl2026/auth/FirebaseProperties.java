package com.bl2026.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "firebase")
public class FirebaseProperties {

    private final Credentials credentials = new Credentials();
    private final Mock mock = new Mock();

    @Getter
    @Setter
    public static class Credentials {

        /**
         * Location of the Firebase service account JSON. Accepts a plain filesystem path,
         * or any Spring resource URL such as {@code classpath:} or {@code file:}.
         */
        private String path;

        /**
         * The service account JSON itself, either raw or base64-encoded. Takes precedence
         * over {@link #path}, because managed hosts hand you environment variables rather
         * than somewhere to put a key file. Base64 spares you having to keep the private
         * key's newlines intact through a dashboard text box.
         */
        private String json;
    }

    @Getter
    @Setter
    public static class Mock {

        private boolean enabled;
        private String token = "waitless-local-token";
        private String firebaseUid = "mock-user-001";
        private String email = "owner@example.com";
        private String displayName = "Demo Owner";
    }
}
