package com.bl2026.staff;

import jakarta.validation.constraints.NotBlank;

public record VerifyPinRequest(@NotBlank String pin) {
}
