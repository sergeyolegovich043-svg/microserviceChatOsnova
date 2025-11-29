package com.example.securechat.domain.service;

import com.example.securechat.domain.dto.MessageReceiptDto;
import com.example.securechat.domain.dto.ReceiptRequest;
import java.util.UUID;

public interface ReceiptService {
    MessageReceiptDto updateReceipt(UUID currentUserId, UUID messageId, ReceiptRequest request);
}
