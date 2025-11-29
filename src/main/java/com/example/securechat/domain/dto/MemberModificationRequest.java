package com.example.securechat.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import java.util.UUID;
import lombok.Data;

@Data
public class MemberModificationRequest {
    @NotEmpty
    private Set<UUID> memberIds;
}
