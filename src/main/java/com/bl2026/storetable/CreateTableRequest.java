package com.bl2026.storetable;

import jakarta.validation.constraints.Positive;

/** The qrToken is generated server-side and is never accepted from the client. */
public record CreateTableRequest(@Positive int tableNumber) {
}
