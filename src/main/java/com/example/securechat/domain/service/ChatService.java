package com.example.securechat.domain.service;

import com.example.securechat.domain.dto.ChatDto;
import com.example.securechat.domain.dto.GroupChatRequest;
import com.example.securechat.domain.dto.PrivateChatRequest;
import com.example.securechat.domain.dto.MemberModificationRequest;
import java.util.List;
import java.util.UUID;

public interface ChatService {
    ChatDto createPrivateChat(UUID currentUserId, PrivateChatRequest request);

    ChatDto createGroupChat(UUID currentUserId, GroupChatRequest request);

    List<ChatDto> getChats(UUID currentUserId);

    ChatDto getChat(UUID currentUserId, UUID chatId);

    ChatDto addMembers(UUID currentUserId, UUID chatId, MemberModificationRequest request);

    ChatDto removeMember(UUID currentUserId, UUID chatId, UUID memberId);
}
