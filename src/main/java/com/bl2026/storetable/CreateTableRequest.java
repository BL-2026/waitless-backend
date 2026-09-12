package com.bl2026.storetable;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** The qrToken is generated server-side and is never accepted from the client. */
public record CreateTableRequest(@Positive int tableNumber, @Size(max = 64) String zone) {
}
