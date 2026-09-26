package com.bl2026.device;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UnregisterDeviceTokenRequest(
        @NotBlank @Size(max = 512) String token) {
}
