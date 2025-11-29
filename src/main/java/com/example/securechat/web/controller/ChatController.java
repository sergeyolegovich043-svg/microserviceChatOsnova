package com.example.securechat.web.controller;

import com.example.securechat.domain.dto.ChatDto;
import com.example.securechat.domain.dto.GroupChatRequest;
import com.example.securechat.domain.dto.MemberModificationRequest;
import com.example.securechat.domain.dto.PrivateChatRequest;
import com.example.securechat.domain.service.ChatService;
import com.example.securechat.security.CurrentUser;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chats")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/private")
    public ResponseEntity<ChatDto> createPrivateChat(@Valid @RequestBody PrivateChatRequest request) {
        ChatDto chat = chatService.createPrivateChat(CurrentUser.getUserId(), request);
        return ResponseEntity.ok(chat);
    }

    @PostMapping("/group")
    public ResponseEntity<ChatDto> createGroupChat(@Valid @RequestBody GroupChatRequest request) {
        ChatDto chat = chatService.createGroupChat(CurrentUser.getUserId(), request);
        return ResponseEntity.ok(chat);
    }

    @GetMapping
    public ResponseEntity<List<ChatDto>> getChats() {
        return ResponseEntity.ok(chatService.getChats(CurrentUser.getUserId()));
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<ChatDto> getChat(@PathVariable UUID chatId) {
        return ResponseEntity.ok(chatService.getChat(CurrentUser.getUserId(), chatId));
    }

    @PostMapping("/{chatId}/members")
    public ResponseEntity<ChatDto> addMembers(@PathVariable UUID chatId,
            @Valid @RequestBody MemberModificationRequest request) {
        return ResponseEntity.ok(chatService.addMembers(CurrentUser.getUserId(), chatId, request));
    }

    @DeleteMapping("/{chatId}/members/{memberId}")
    public ResponseEntity<ChatDto> removeMember(@PathVariable UUID chatId, @PathVariable UUID memberId) {
        return ResponseEntity.ok(chatService.removeMember(CurrentUser.getUserId(), chatId, memberId));
    }
}
