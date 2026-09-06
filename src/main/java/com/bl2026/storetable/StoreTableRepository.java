package com.bl2026.storetable;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreTableRepository extends JpaRepository<StoreTable, UUID> {

    Optional<StoreTable> findByQrToken(String qrToken);

    List<StoreTable> findByStoreIdOrderByTableNumberAsc(UUID storeId);
}
