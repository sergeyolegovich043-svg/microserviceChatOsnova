package com.example.securechat.domain.dto;

import com.example.securechat.domain.model.ReceiptStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageReceiptDto {
    private UUID id;
    private UUID messageId;
    private UUID userId;
    private ReceiptStatus status;
    private Instant updatedAt;
}
