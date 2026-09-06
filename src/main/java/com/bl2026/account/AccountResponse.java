package com.bl2026.account;

import java.time.Instant;
import java.util.UUID;

public record AccountResponse(UUID id, String firebaseUid, String fullName, String email, Instant createdAt) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(account.getId(), account.getFirebaseUid(), account.getFullName(), account.getEmail(),
                account.getCreatedAt());
    }
}
