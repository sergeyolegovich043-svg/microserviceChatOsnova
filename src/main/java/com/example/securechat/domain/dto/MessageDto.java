package com.example.securechat.domain.dto;

import com.example.securechat.domain.model.MessageType;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageDto {
    private UUID id;
    private UUID chatId;
    private UUID senderId;
    private MessageType type;
    private String encryptedPayload;
    private String encryptedMediaKey;
    private String mediaUrl;
    private Instant createdAt;
    private Instant editedAt;
    private Instant deletedAt;
}
