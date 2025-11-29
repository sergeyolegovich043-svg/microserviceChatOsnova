package com.example.securechat.domain.dto;

import com.example.securechat.domain.model.ReceiptStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReceiptRequest {
    @NotNull
    private ReceiptStatus status;
}
