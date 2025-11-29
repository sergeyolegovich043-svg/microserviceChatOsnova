package com.example.securechat.domain.service.impl;

import com.example.securechat.domain.dto.MessageReceiptDto;
import com.example.securechat.domain.dto.ReceiptRequest;
import com.example.securechat.domain.model.ChatMember;
import com.example.securechat.domain.model.Message;
import com.example.securechat.domain.model.MessageReceipt;
import com.example.securechat.domain.repository.ChatMemberRepository;
import com.example.securechat.domain.repository.MessageReceiptRepository;
import com.example.securechat.domain.repository.MessageRepository;
import com.example.securechat.domain.service.WebSocketNotificationService;
import com.example.securechat.exception.ForbiddenException;
import com.example.securechat.exception.NotFoundException;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReceiptServiceImpl implements com.example.securechat.domain.service.ReceiptService {

    private final MessageRepository messageRepository;
    private final MessageReceiptRepository receiptRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final WebSocketNotificationService notificationService;

    public ReceiptServiceImpl(MessageRepository messageRepository, MessageReceiptRepository receiptRepository,
            ChatMemberRepository chatMemberRepository, WebSocketNotificationService notificationService) {
        this.messageRepository = messageRepository;
        this.receiptRepository = receiptRepository;
        this.chatMemberRepository = chatMemberRepository;
        this.notificationService = notificationService;
    }

    @Override
    public MessageReceiptDto updateReceipt(UUID currentUserId, UUID messageId, ReceiptRequest request) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Message not found"));
        ChatMember member = chatMemberRepository.findByChatIdAndUserId(message.getChat().getId(), currentUserId)
                .orElseThrow(() -> new ForbiddenException("User is not a member of this chat"));

        MessageReceipt receipt = receiptRepository.findByMessageIdAndUserId(messageId, currentUserId)
                .orElseGet(() -> {
                    MessageReceipt mr = new MessageReceipt();
                    mr.setId(UUID.randomUUID());
                    mr.setMessage(message);
                    mr.setUserId(currentUserId);
                    return mr;
                });
        receipt.setStatus(request.getStatus());
        receipt.setUpdatedAt(Instant.now());
        receiptRepository.save(receipt);
        MessageReceiptDto dto = MessageReceiptDto.builder()
                .id(receipt.getId())
                .messageId(messageId)
                .userId(currentUserId)
                .status(receipt.getStatus())
                .updatedAt(receipt.getUpdatedAt())
                .build();
        notificationService.broadcastReceiptUpdate(message.getSenderId(), dto);
        return dto;
    }
}
