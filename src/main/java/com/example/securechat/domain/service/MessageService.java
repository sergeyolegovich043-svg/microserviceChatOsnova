package com.example.securechat.domain.service;

import com.example.securechat.domain.dto.MessageCreateRequest;
import com.example.securechat.domain.dto.MessageDto;
import java.util.List;
import java.util.UUID;

public interface MessageService {
    List<MessageDto> getMessages(UUID userId, UUID chatId, int limit, UUID beforeMessageId);

    MessageDto sendMessage(UUID userId, UUID chatId, MessageCreateRequest request);
}
