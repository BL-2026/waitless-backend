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

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Account(String firebaseUid, String fullName, String email) {
        this.firebaseUid = firebaseUid;
        this.fullName = fullName;
        this.email = email;
        this.createdAt = Instant.now();
    }
}
