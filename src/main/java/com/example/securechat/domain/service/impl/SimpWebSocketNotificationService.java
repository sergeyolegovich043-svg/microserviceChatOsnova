package com.example.securechat.domain.service.impl;

import com.example.securechat.domain.dto.MessageDto;
import com.example.securechat.domain.dto.MessageReceiptDto;
import com.example.securechat.domain.service.WebSocketNotificationService;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class SimpWebSocketNotificationService implements WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public SimpWebSocketNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void broadcastNewMessage(UUID chatId, MessageDto message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "NEW_MESSAGE");
        payload.put("payload", message);
        messagingTemplate.convertAndSend("/topic/chats/" + chatId, payload);
    }

    @Override
    public void broadcastReceiptUpdate(UUID recipientUserId, MessageReceiptDto receipt) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "RECEIPT_UPDATED");
        payload.put("payload", receipt);
        messagingTemplate.convertAndSend("/topic/users/" + recipientUserId, payload);
    }

    @Override
    public void broadcastTyping(UUID chatId, UUID userId, boolean typing) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "TYPING");
        payload.put("chatId", chatId);
        payload.put("userId", userId);
        payload.put("isTyping", typing);
        messagingTemplate.convertAndSend("/topic/chats/" + chatId, payload);
    }
}
