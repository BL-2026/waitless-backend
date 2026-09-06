package com.bl2026.staff;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** The plaintext PIN is hashed before it is stored and is never returned by the API. */
public record CreateStaffRequest(
        @NotBlank String fullName,
        @Pattern(regexp = "\\d{4}", message = "must be exactly 4 digits") String pin) {
}
