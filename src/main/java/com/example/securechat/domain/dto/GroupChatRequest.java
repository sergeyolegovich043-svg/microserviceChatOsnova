package com.example.securechat.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import java.util.UUID;
import lombok.Data;

@Data
public class GroupChatRequest {
    @NotBlank
    private String title;
    @NotEmpty
    private Set<UUID> memberIds;
}
