package com.example.securechat.domain.dto;

import com.example.securechat.domain.model.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MediaPresignRequest {
    @NotNull
    private MessageType mediaType;
    @NotBlank
    private String fileName;
    @NotBlank
    private String contentType;
}
