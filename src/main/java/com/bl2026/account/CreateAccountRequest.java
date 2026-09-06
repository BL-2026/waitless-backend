package com.bl2026.account;

/**
 * Body for {@code POST /api/accounts}. Both fields are optional: when omitted, the values
 * are taken from the verified Firebase ID token.
 */
public record CreateAccountRequest(String fullName, String email) {
}
