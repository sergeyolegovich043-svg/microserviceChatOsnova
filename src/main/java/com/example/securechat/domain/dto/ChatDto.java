package com.example.securechat.domain.dto;

import com.example.securechat.domain.model.ChatType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatDto {
    private UUID id;
    private ChatType type;
    private String title;
    private UUID createdBy;
    private Instant createdAt;
    private Instant updatedAt;
    private List<ChatMemberDto> members;
    private MessageDto lastMessage;
}
