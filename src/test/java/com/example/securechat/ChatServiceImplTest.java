package com.example.securechat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.securechat.domain.dto.ChatDto;
import com.example.securechat.domain.dto.PrivateChatRequest;
import com.example.securechat.domain.model.Chat;
import com.example.securechat.domain.model.ChatType;
import com.example.securechat.domain.repository.ChatMemberRepository;
import com.example.securechat.domain.repository.ChatRepository;
import com.example.securechat.domain.repository.MessageRepository;
import com.example.securechat.domain.service.impl.ChatServiceImpl;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ChatServiceImplTest {

    private ChatServiceImpl chatService;
    @Mock
    private ChatRepository chatRepository;
    @Mock
    private ChatMemberRepository chatMemberRepository;
    @Mock
    private MessageRepository messageRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        chatService = new ChatServiceImpl(chatRepository, chatMemberRepository, messageRepository);
    }

    @Test
    void createsPrivateChatWhenMissing() {
        UUID currentUser = UUID.randomUUID();
        UUID peer = UUID.randomUUID();
        when(chatRepository.findPrivateChatBetween(ChatType.PRIVATE, currentUser, peer)).thenReturn(Optional.empty());
        when(messageRepository.findTop1ByChatIdOrderByCreatedAtDesc(any())).thenReturn(Optional.empty());
        PrivateChatRequest request = new PrivateChatRequest();
        request.setPeerUserId(peer);
        ChatDto dto = chatService.createPrivateChat(currentUser, request);
        assertThat(dto.getType()).isEqualTo(ChatType.PRIVATE);
        assertThat(dto.getMembers()).hasSize(2);
    }

    @Test
    void returnsExistingPrivateChat() {
        UUID currentUser = UUID.randomUUID();
        UUID peer = UUID.randomUUID();
        Chat chat = new Chat();
        chat.setId(UUID.randomUUID());
        chat.setType(ChatType.PRIVATE);
        when(chatRepository.findPrivateChatBetween(ChatType.PRIVATE, currentUser, peer)).thenReturn(Optional.of(chat));
        when(messageRepository.findTop1ByChatIdOrderByCreatedAtDesc(chat.getId())).thenReturn(Optional.empty());
        ChatDto dto = chatService.createPrivateChat(currentUser, buildRequest(peer));
        assertThat(dto.getId()).isEqualTo(chat.getId());
    }

    private PrivateChatRequest buildRequest(UUID peer) {
        PrivateChatRequest request = new PrivateChatRequest();
        request.setPeerUserId(peer);
        return request;
    }
}
