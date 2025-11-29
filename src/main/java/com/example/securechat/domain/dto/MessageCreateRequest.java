package com.example.securechat.domain.dto;

import com.example.securechat.domain.model.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageCreateRequest {
    @NotNull
    private MessageType type;
    @NotBlank
    private String encryptedPayload;
    private String encryptedMediaKey;
    private String mediaUrl;
    private String clientMessageId;
}
