package com.bl2026.storetable;

import com.bl2026.menu.MenuItemResponse;

import java.util.List;
import java.util.UUID;

/** What the customer web app gets after scanning a QR code: the table, its store, and the menu. */
public record TableResolutionResponse(TableInfo table, StoreInfo store, List<MenuItemResponse> menu) {

    public record TableInfo(UUID id, int tableNumber) {
    }

    public record StoreInfo(UUID id, String name) {
    }
}
