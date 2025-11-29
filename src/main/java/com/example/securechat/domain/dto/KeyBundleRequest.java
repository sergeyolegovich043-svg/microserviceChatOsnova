package com.example.securechat.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class KeyBundleRequest {
    @NotNull
    private UUID deviceId;
    @NotBlank
    private String identityKeyPublic;
    @NotBlank
    private String signedPreKeyPublic;
    @NotNull
    private List<String> oneTimePreKeysPublic;
}
