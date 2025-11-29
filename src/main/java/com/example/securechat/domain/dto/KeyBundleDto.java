package com.example.securechat.domain.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KeyBundleDto {
    private UUID id;
    private UUID userId;
    private UUID deviceId;
    private String identityKeyPublic;
    private String signedPreKeyPublic;
    private List<String> oneTimePreKeysPublic;
    private Instant updatedAt;
}
