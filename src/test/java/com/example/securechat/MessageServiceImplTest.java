package com.example.securechat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.securechat.domain.dto.MessageCreateRequest;
import com.example.securechat.domain.dto.MessageDto;
import com.example.securechat.domain.model.Chat;
import com.example.securechat.domain.model.ChatMember;
import com.example.securechat.domain.model.ChatType;
import com.example.securechat.domain.model.MemberRole;
import com.example.securechat.domain.model.Message;
import com.example.securechat.domain.model.MessageType;
import com.example.securechat.domain.repository.ChatMemberRepository;
import com.example.securechat.domain.repository.ChatRepository;
import com.example.securechat.domain.repository.MessageRepository;
import com.example.securechat.domain.service.WebSocketNotificationService;
import com.example.securechat.domain.service.impl.MessageServiceImpl;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class MessageServiceImplTest {

    private MessageServiceImpl service;
    @Mock
    private ChatRepository chatRepository;
    @Mock
    private ChatMemberRepository chatMemberRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private WebSocketNotificationService notificationService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new MessageServiceImpl(chatRepository, chatMemberRepository, messageRepository, notificationService);
    }

    @Test
    void sendsCiphertextMessage() {
        UUID chatId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Chat chat = new Chat();
        chat.setId(chatId);
        chat.setType(ChatType.PRIVATE);
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        ChatMember member = new ChatMember();
        member.setRole(MemberRole.MEMBER);
        when(chatMemberRepository.findByChatIdAndUserId(chatId, userId)).thenReturn(Optional.of(member));
        Message saved = new Message();
        saved.setId(UUID.randomUUID());
        saved.setChat(chat);
        saved.setSenderId(userId);
        saved.setType(MessageType.TEXT);
        saved.setEncryptedPayload("ciphertext");
        when(messageRepository.save(any())).thenReturn(saved);
        MessageCreateRequest request = new MessageCreateRequest();
        request.setType(MessageType.TEXT);
        request.setEncryptedPayload("ciphertext");
        MessageDto dto = service.sendMessage(userId, chatId, request);
        assertThat(dto.getEncryptedPayload()).isEqualTo("ciphertext");
        assertThat(dto.getType()).isEqualTo(MessageType.TEXT);
    }
}
