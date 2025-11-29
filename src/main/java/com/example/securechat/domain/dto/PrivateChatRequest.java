package com.example.securechat.domain.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;

@Data
public class PrivateChatRequest {
    @NotNull
    private UUID peerUserId;
}
