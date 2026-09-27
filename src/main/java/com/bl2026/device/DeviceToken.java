package com.bl2026.device;

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

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "device_token")
@Getter
@Setter
@NoArgsConstructor
public class DeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "token", nullable = false, unique = true, length = 512)
    private String token;

    @Column(name = "locale", nullable = false, length = 8)
    private String locale;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public DeviceToken(Store store, String token, String locale) {
        this.store = store;
        this.token = token;
        this.locale = locale;
        this.updatedAt = Instant.now();
    }

    public void reassign(Store store, String locale) {
        this.store = store;
        this.locale = locale;
        this.updatedAt = Instant.now();
    }
}
