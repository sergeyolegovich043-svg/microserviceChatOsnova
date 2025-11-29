package com.example.securechat.domain.repository;

import com.example.securechat.domain.model.SessionMetadata;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionMetadataRepository extends JpaRepository<SessionMetadata, UUID> {
    List<SessionMetadata> findByChatId(UUID chatId);

    List<SessionMetadata> findByUserId(UUID userId);
}
