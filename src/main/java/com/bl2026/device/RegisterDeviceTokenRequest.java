package com.bl2026.device;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterDeviceTokenRequest(
        @NotBlank @Size(max = 512) String token,
        @NotBlank @Size(max = 8) String locale) {
}
