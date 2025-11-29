package com.example.securechat.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_public_key_bundles")
@Getter
@Setter
@NoArgsConstructor
public class UserPublicKeyBundle {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID deviceId;

    @Column(nullable = false, length = 2048)
    private String identityKeyPublic;

    @Column(nullable = false, length = 2048)
    private String signedPreKeyPublic;

    @Column(nullable = false, length = 4096)
    private String oneTimePreKeysPublic;

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();
}
