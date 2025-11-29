package com.example.securechat.web.controller;

import com.example.securechat.domain.dto.MessageReceiptDto;
import com.example.securechat.domain.dto.ReceiptRequest;
import com.example.securechat.domain.service.ReceiptService;
import com.example.securechat.security.CurrentUser;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/messages")
public class ReceiptController {

    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @PostMapping("/{messageId}/receipts")
    public ResponseEntity<MessageReceiptDto> updateReceipt(@PathVariable UUID messageId,
            @Valid @RequestBody ReceiptRequest request) {
        return ResponseEntity.ok(receiptService.updateReceipt(CurrentUser.getUserId(), messageId, request));
    }
}
