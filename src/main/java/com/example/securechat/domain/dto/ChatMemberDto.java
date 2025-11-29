package com.example.securechat.domain.dto;

import com.example.securechat.domain.model.MemberRole;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatMemberDto {
    private UUID id;
    private UUID userId;
    private MemberRole role;
    private Instant joinedAt;
    private boolean muted;
}
