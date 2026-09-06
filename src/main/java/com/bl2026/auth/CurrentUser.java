package com.bl2026.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Reads the verified Firebase identity out of the {@code SecurityContext}. */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static FirebaseAuthenticationToken require() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof FirebaseAuthenticationToken firebaseToken && firebaseToken.isAuthenticated()) {
            return firebaseToken;
        }
        throw new IllegalStateException("No authenticated Firebase user in the security context");
    }

    public static String requireFirebaseUid() {
        return require().getFirebaseUid();
    }
}
