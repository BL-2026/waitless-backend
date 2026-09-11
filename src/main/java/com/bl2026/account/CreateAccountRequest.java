package com.bl2026.account;

/**
 * Body for {@code POST /api/account}. Every field is optional: {@code fullName} and
 * {@code email} fall back to the verified Firebase ID token when blank. {@code phoneArea}
 * is the dial code ({@code +212}) and {@code phoneNumber} the national part ({@code 612345678}).
 */
public record CreateAccountRequest(String fullName, String phoneArea, String phoneNumber, String email) {
}
