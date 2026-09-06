package com.bl2026.store;

import jakarta.validation.constraints.NotBlank;

public record CreateStoreRequest(@NotBlank String name) {
}
