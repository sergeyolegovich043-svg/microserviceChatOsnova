package com.example.securechat.domain.service;

import com.example.securechat.domain.dto.MessageDto;
import com.example.securechat.domain.dto.MessageReceiptDto;
import java.util.UUID;

public interface WebSocketNotificationService {
    void broadcastNewMessage(UUID chatId, MessageDto message);

    void broadcastReceiptUpdate(UUID recipientUserId, MessageReceiptDto receipt);

    void broadcastTyping(UUID chatId, UUID userId, boolean typing);
}
