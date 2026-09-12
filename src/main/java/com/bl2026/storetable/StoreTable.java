package com.bl2026.storetable;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "store_table")
@Getter
@Setter
@NoArgsConstructor
public class StoreTable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    /** Human-facing number printed on the table. Unique within a store. */
    @Column(name = "table_number", nullable = false)
    private int tableNumber;

    /** Free-form area label ("terrace", "salon"). Optional; not a separate table yet. */
    @Column(name = "zone", length = 64)
    private String zone;

    /**
     * Opaque random token embedded in the QR code URL. Deliberately not derived from
     * {@link #tableNumber} so a customer cannot guess another table's identity.
     */
    @Column(name = "qr_token", nullable = false, unique = true, length = 64, updatable = false)
    private String qrToken;

    public StoreTable(Store store, int tableNumber, String zone, String qrToken) {
        this.store = store;
        this.tableNumber = tableNumber;
        this.zone = zone;
        this.qrToken = qrToken;
    }
}
