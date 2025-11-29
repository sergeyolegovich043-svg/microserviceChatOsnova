package com.example.securechat.web.websocket;

import com.example.securechat.domain.service.WebSocketNotificationService;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    private final WebSocketNotificationService notificationService;

    public ChatWebSocketController(WebSocketNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @MessageMapping("typing")
    public void handleTyping(@Payload Map<String, Object> payload, Principal principal) {
        if (principal == null) {
            return;
        }
        UUID userId = UUID.fromString(principal.getName());
        UUID chatId = UUID.fromString(payload.get("chatId").toString());
        boolean isTyping = Boolean.parseBoolean(payload.getOrDefault("isTyping", Boolean.FALSE).toString());
        notificationService.broadcastTyping(chatId, userId, isTyping);
    }
}
