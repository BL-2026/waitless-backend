package com.bl2026.account;

import java.time.Instant;
import java.util.UUID;

public record AccountResponse(UUID id, String firebaseUid, String fullName, String phoneArea, String phoneNumber, String email, Instant createdAt) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(account.getId(), account.getFirebaseUid(), account.getFullName(), account.getPhoneArea(),
                account.getPhoneNumber(), account.getEmail(), account.getCreatedAt());
    }
}
