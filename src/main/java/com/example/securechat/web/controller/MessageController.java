package com.example.securechat.web.controller;

import com.example.securechat.domain.dto.MessageCreateRequest;
import com.example.securechat.domain.dto.MessageDto;
import com.example.securechat.domain.service.MessageService;
import com.example.securechat.security.CurrentUser;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/api/v1/chats/{chatId}/messages")
    public ResponseEntity<List<MessageDto>> getMessages(@PathVariable UUID chatId,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) UUID beforeMessageId) {
        return ResponseEntity.ok(messageService.getMessages(CurrentUser.getUserId(), chatId, limit, beforeMessageId));
    }

    @PostMapping("/api/v1/chats/{chatId}/messages")
    public ResponseEntity<MessageDto> sendMessage(@PathVariable UUID chatId,
            @Valid @RequestBody MessageCreateRequest request) {
        return ResponseEntity.ok(messageService.sendMessage(CurrentUser.getUserId(), chatId, request));
    }
}
