package com.bl2026.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "account")
@Getter
@Setter
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /** Firebase Auth UID of the account; created by the Flutter app at signup. */
    @Column(name = "firebase_uid", nullable = false, unique = true, length = 128, updatable = false)
    private String firebaseUid;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    /** Dial code including the plus sign, e.g. {@code +212}. */
    @Column(name = "phone_area", length = 8)
    private String phoneArea;

    /** National part only, without the dial code, e.g. {@code 612345678}. */
    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Account(String firebaseUid, String fullName, String phoneArea, String phoneNumber, String email) {
        this.firebaseUid = firebaseUid;
        this.fullName = fullName;
        this.phoneArea = phoneArea;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.createdAt = Instant.now();
    }
}
