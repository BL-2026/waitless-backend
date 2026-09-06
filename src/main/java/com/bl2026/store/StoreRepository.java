package com.bl2026.store;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {

    List<Store> findByAccountIdOrderByCreatedAtAsc(UUID accountId);

    Optional<Store> findByIdAndAccountId(UUID id, UUID accountId);
}
