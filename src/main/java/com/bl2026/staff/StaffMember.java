package com.bl2026.staff;

import com.bl2026.store.Store;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * A staff member has no Firebase login of their own. The account's device is already
 * authenticated; the PIN only says which staff member is acting on that device, so that
 * acknowledging a {@code ServiceRequest} can be attributed to someone.
 */
@Entity
@Table(name = "staff_member", uniqueConstraints = @UniqueConstraint(
        name = "uq_staff_member_store_pin", columnNames = {"store_id", "pin"}))
@Getter
@Setter
@NoArgsConstructor
public class StaffMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "pin", nullable = false, length = 4)
    private String pin;

    public StaffMember(Store store, String fullName, String pin) {
        this.store = store;
        this.fullName = fullName;
        this.pin = pin;
    }
}
