package com.bl2026.auth;

import com.google.firebase.auth.FirebaseToken;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.List;

/**
 * Authentication holding a verified Firebase identity. The principal is the Firebase UID,
 * which is what account-facing controllers use to resolve the current {@code Account}.
 */
@Getter
public class FirebaseAuthenticationToken extends AbstractAuthenticationToken {

    private final String firebaseUid;
    private final String email;
    private final String displayName;

    public FirebaseAuthenticationToken(FirebaseToken token) {
        super(List.of());
        this.firebaseUid = token.getUid();
        this.email = token.getEmail();
        this.displayName = token.getName();
        setAuthenticated(true);
    }

    public FirebaseAuthenticationToken(String firebaseUid, String email, String displayName) {
        super(List.of());
        this.firebaseUid = firebaseUid;
        this.email = email;
        this.displayName = displayName;
        setAuthenticated(true);
    }

    @Override
    public Object getPrincipal() {
        return firebaseUid;
    }

    @Override
    public Object getCredentials() {
        return "";
    }
}
