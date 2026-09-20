package com.bl2026.staff;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateStaffRequest(
        @NotBlank String fullName,
        @Pattern(regexp = "\\d{4}", message = "must be exactly 4 digits") String pin) {
}
