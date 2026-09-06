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
         * Empty means Firebase verification is disabled.
         */
        private String path;
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
