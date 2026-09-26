package com.bl2026.common;

import java.time.Instant;

/**
 * HTTP error envelope. {@code code} is what clients localize; {@code message} is for logs
 * and debugging and may stay English.
 */
public record ApiError(Instant timestamp, int status, String error, String code, String message) {

    public static ApiError of(int status, String error, String code, String message) {
        return new ApiError(Instant.now(), status, error, code, message);
    }
}
