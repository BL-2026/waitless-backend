package com.bl2026.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

/**
 * Public body for {@code POST /api/requests}.
 *
 * <p>{@code storeId} and {@code tableNumber} state which tenant and table the customer
 * believes they are sitting at. {@code qrToken} is the credential that proves it: the
 * server resolves the token independently and rejects the call when the two disagree.
 * Without that check these ids would be forgeable, since this endpoint is unauthenticated
 * and store ids are visible to any customer.
 */
public record CreateServiceRequestRequest(
        @NotNull UUID storeId,
        @Positive int tableNumber,
        @NotBlank String qrToken,
        @NotNull RequestType type,
        PaymentMethod paymentMethod) {
}
