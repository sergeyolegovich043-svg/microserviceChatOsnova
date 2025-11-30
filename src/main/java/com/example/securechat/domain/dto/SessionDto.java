package com.example.securechat.domain.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SessionDto {

    private UUID id;
    private UUID chatId;
    private UUID userId;
    private String clientSessionId;
    private Instant createdAt;
    private KeyBundleDto keyBundle;
}
