package com.bl2026.storetable;

import java.util.UUID;

public record StoreTableResponse(UUID id, int tableNumber, String zone, String qrToken) {

    public static StoreTableResponse from(StoreTable table) {
        return new StoreTableResponse(table.getId(), table.getTableNumber(), table.getZone(), table.getQrToken());
    }
}
