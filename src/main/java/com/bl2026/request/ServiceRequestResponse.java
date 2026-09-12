package com.bl2026.request;

import com.bl2026.staff.StaffMember;

import java.time.Instant;
import java.util.UUID;

public record ServiceRequestResponse(
        UUID id,
        UUID storeId,
        UUID tableId,
        int tableNumber,
        String zone,
        RequestType type,
        RequestStatus status,
        PaymentMethod paymentMethod,
        Instant createdAt,
        UUID acknowledgedById,
        String acknowledgedByName,
        Instant acknowledgedAt,
        Instant resolvedAt) {

    public static ServiceRequestResponse from(ServiceRequest request) {
        StaffMember acknowledgedBy = request.getAcknowledgedBy();
        return new ServiceRequestResponse(
                request.getId(),
                request.getStore().getId(),
                request.getTable().getId(),
                request.getTable().getTableNumber(),
                request.getTable().getZone(),
                request.getType(),
                request.getStatus(),
                request.getPaymentMethod(),
                request.getCreatedAt(),
                acknowledgedBy == null ? null : acknowledgedBy.getId(),
                acknowledgedBy == null ? null : acknowledgedBy.getFullName(),
                request.getAcknowledgedAt(),
                request.getResolvedAt());
    }
}
