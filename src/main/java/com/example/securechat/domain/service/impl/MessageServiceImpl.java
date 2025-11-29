package com.example.securechat.domain.service.impl;

import com.example.securechat.domain.dto.MessageCreateRequest;
import com.example.securechat.domain.dto.MessageDto;
import com.example.securechat.domain.model.Chat;
import com.example.securechat.domain.model.ChatMember;
import com.example.securechat.domain.model.Message;
import com.example.securechat.domain.repository.ChatMemberRepository;
import com.example.securechat.domain.repository.ChatRepository;
import com.example.securechat.domain.repository.MessageRepository;
import com.example.securechat.domain.service.WebSocketNotificationService;
import com.example.securechat.exception.ForbiddenException;
import com.example.securechat.exception.NotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MessageServiceImpl implements com.example.securechat.domain.service.MessageService {

    private final ChatRepository chatRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final MessageRepository messageRepository;
    private final WebSocketNotificationService notificationService;

    public MessageServiceImpl(ChatRepository chatRepository, ChatMemberRepository chatMemberRepository,
            MessageRepository messageRepository, WebSocketNotificationService notificationService) {
        this.chatRepository = chatRepository;
        this.chatMemberRepository = chatMemberRepository;
        this.messageRepository = messageRepository;
        this.notificationService = notificationService;
    }

    @Override
    public List<MessageDto> getMessages(UUID userId, UUID chatId, int limit, UUID beforeMessageId) {
        ensureMember(chatId, userId);
        List<Message> messages;
        if (beforeMessageId != null) {
            messages = messageRepository.findByChatIdAndBeforeMessageId(chatId, beforeMessageId, PageRequest.of(0, limit));
        } else {
            messages = messageRepository.findByChatIdOrderByCreatedAtDesc(chatId, PageRequest.of(0, limit));
        }
        return messages.stream().map(this::toDto).toList();
    }

    @Override
    public MessageDto sendMessage(UUID userId, UUID chatId, MessageCreateRequest request) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new NotFoundException("Chat not found"));
        ensureMember(chatId, userId);
        Message message = new Message();
        message.setId(UUID.randomUUID());
        message.setChat(chat);
        message.setSenderId(userId);
        message.setType(request.getType());
        message.setEncryptedPayload(request.getEncryptedPayload());
        message.setEncryptedMediaKey(request.getEncryptedMediaKey());
        message.setMediaUrl(request.getMediaUrl());
        message.setCreatedAt(Instant.now());
        messageRepository.save(message);
        MessageDto dto = toDto(message);
        notificationService.broadcastNewMessage(chatId, dto);
        return dto;
    }

    private void ensureMember(UUID chatId, UUID userId) {
        ChatMember member = chatMemberRepository.findByChatIdAndUserId(chatId, userId)
                .orElseThrow(() -> new ForbiddenException("User is not a member of this chat"));
    }

    private MessageDto toDto(Message message) {
        return MessageDto.builder()
                .id(message.getId())
                .chatId(message.getChat().getId())
                .senderId(message.getSenderId())
                .type(message.getType())
                .encryptedPayload(message.getEncryptedPayload())
                .encryptedMediaKey(message.getEncryptedMediaKey())
                .mediaUrl(message.getMediaUrl())
                .createdAt(message.getCreatedAt())
                .editedAt(message.getEditedAt())
                .deletedAt(message.getDeletedAt())
                .build();
    }
}
