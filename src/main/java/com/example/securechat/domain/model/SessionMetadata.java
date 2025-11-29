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

/**
 * Stores only non-sensitive session metadata that helps correlate client-created Signal sessions.
 * No secret keys or ratchet state should be persisted on the server.
 */
@Entity
@Table(name = "session_metadata")
@Getter
@Setter
@NoArgsConstructor
public class SessionMetadata {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID chatId;

    @Column(nullable = false)
    private UUID userId;

    private String clientSessionId;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
