package com.example.securechat.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;

@Data
public class SessionInitRequest {

    @NotNull
    private UUID deviceId;

    @NotBlank
    private String clientSessionId;
}
