package com.bl2026.menu;

import java.math.BigDecimal;
import java.util.UUID;

public record MenuItemResponse(UUID id, String category, String name, String description, BigDecimal price) {

    public static MenuItemResponse from(MenuItem item) {
        return new MenuItemResponse(item.getId(), item.getCategory(), item.getName(), item.getDescription(),
                item.getPrice());
    }
}
