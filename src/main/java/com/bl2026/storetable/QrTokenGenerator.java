package com.bl2026.storetable;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

/** Produces the unguessable tokens embedded in table QR codes. */
@Component
public class QrTokenGenerator {

    private static final int TOKEN_BYTES = 24;

    private final SecureRandom random = new SecureRandom();
    private final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    public String generate() {
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        return encoder.encodeToString(bytes);
    }
}
