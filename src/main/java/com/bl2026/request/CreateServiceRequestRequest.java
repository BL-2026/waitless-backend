package com.bl2026.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Public body for {@code POST /api/requests}. The customer identifies their table by the
 * QR token only; store and table ids are never accepted from the client.
 */
public record CreateServiceRequestRequest(
        @NotBlank String qrToken,
        @NotNull RequestType type,
        PaymentMethod paymentMethod) {
}
