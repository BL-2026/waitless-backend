package com.bl2026.request;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, UUID> {

    List<ServiceRequest> findByStoreIdAndStatusInOrderByCreatedAtAsc(UUID storeId, Collection<RequestStatus> statuses);
}
