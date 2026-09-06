package com.bl2026.request;

import com.bl2026.staff.StaffMember;
import com.bl2026.store.Store;
import com.bl2026.storetable.StoreTable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "service_request")
@Getter
@Setter
@NoArgsConstructor
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "table_id", nullable = false)
    private StoreTable table;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    private RequestType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private RequestStatus status;

    /** Only set when {@link #type} is {@link RequestType#REQUEST_BILL}. */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 32)
    private PaymentMethod paymentMethod;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acknowledged_by_id")
    private StaffMember acknowledgedBy;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    public ServiceRequest(Store store, StoreTable table, RequestType type, PaymentMethod paymentMethod) {
        this.store = store;
        this.table = table;
        this.type = type;
        this.paymentMethod = paymentMethod;
        this.status = RequestStatus.OPEN;
        this.createdAt = Instant.now();
    }

    public void acknowledge(StaffMember staff) {
        this.status = RequestStatus.ACKNOWLEDGED;
        this.acknowledgedBy = staff;
        this.acknowledgedAt = Instant.now();
    }

    public void resolve() {
        this.status = RequestStatus.RESOLVED;
        this.resolvedAt = Instant.now();
    }
}
