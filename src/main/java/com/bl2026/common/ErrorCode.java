package com.bl2026.common;

/**
 * Stable machine codes for clients to localize. English {@code message} stays for logs;
 * UIs must not depend on it.
 */
public final class ErrorCode {

    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String BAD_REQUEST = "BAD_REQUEST";
    public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    public static final String FORBIDDEN = "FORBIDDEN";
    public static final String CONFLICT = "CONFLICT";
    public static final String AUTH_MISSING = "AUTH_MISSING";
    public static final String AUTH_INVALID = "AUTH_INVALID";
    public static final String AUTH_UNAVAILABLE = "AUTH_UNAVAILABLE";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    private ErrorCode() {
    }
}
