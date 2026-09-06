package com.bl2026.menu;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateMenuItemRequest(
        @NotBlank String category,
        @NotBlank String name,
        String description,
        @NotNull @PositiveOrZero BigDecimal price) {
}
