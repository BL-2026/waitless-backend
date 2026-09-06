package com.bl2026.store;

import java.time.Instant;
import java.util.UUID;

public record StoreResponse(UUID id, String name, Instant createdAt) {

    public static StoreResponse from(Store store) {
        return new StoreResponse(store.getId(), store.getName(), store.getCreatedAt());
    }
}
