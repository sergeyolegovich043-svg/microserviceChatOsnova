package com.example.securechat.domain.service.impl;

import com.example.securechat.domain.dto.ChatDto;
import com.example.securechat.domain.dto.ChatMemberDto;
import com.example.securechat.domain.dto.GroupChatRequest;
import com.example.securechat.domain.dto.MemberModificationRequest;
import com.example.securechat.domain.dto.MessageDto;
import com.example.securechat.domain.dto.PrivateChatRequest;
import com.example.securechat.domain.model.Chat;
import com.example.securechat.domain.model.ChatMember;
import com.example.securechat.domain.model.ChatType;
import com.example.securechat.domain.model.MemberRole;
import com.example.securechat.domain.model.Message;
import com.example.securechat.domain.repository.ChatMemberRepository;
import com.example.securechat.domain.repository.ChatRepository;
import com.example.securechat.domain.repository.MessageRepository;
import com.example.securechat.exception.ForbiddenException;
import com.example.securechat.exception.NotFoundException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ChatServiceImpl implements com.example.securechat.domain.service.ChatService {

    private final ChatRepository chatRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final MessageRepository messageRepository;

    public ChatServiceImpl(ChatRepository chatRepository, ChatMemberRepository chatMemberRepository,
            MessageRepository messageRepository) {
        this.chatRepository = chatRepository;
        this.chatMemberRepository = chatMemberRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    public ChatDto createPrivateChat(UUID currentUserId, PrivateChatRequest request) {
        return chatRepository.findPrivateChatBetween(ChatType.PRIVATE, currentUserId, request.getPeerUserId())
                .map(chat -> toDto(chat, currentUserId))
                .orElseGet(() -> {
                    Chat chat = new Chat();
                    chat.setId(UUID.randomUUID());
                    chat.setType(ChatType.PRIVATE);
                    chat.setCreatedBy(currentUserId);
                    chat.setCreatedAt(Instant.now());
                    chat.setUpdatedAt(Instant.now());
                    chatRepository.save(chat);

                    List<ChatMember> members = new ArrayList<>();
                    members.add(createMember(chat, currentUserId, MemberRole.MEMBER));
                    members.add(createMember(chat, request.getPeerUserId(), MemberRole.MEMBER));
                    chatMemberRepository.saveAll(members);
                    chat.setMembers(members);
                    return toDto(chat, currentUserId);
                });
    }

    @Override
    public ChatDto createGroupChat(UUID currentUserId, GroupChatRequest request) {
        Chat chat = new Chat();
        chat.setId(UUID.randomUUID());
        chat.setType(ChatType.GROUP);
        chat.setTitle(request.getTitle());
        chat.setCreatedBy(currentUserId);
        chat.setCreatedAt(Instant.now());
        chat.setUpdatedAt(Instant.now());
        chatRepository.save(chat);

        Set<UUID> memberIds = request.getMemberIds();
        memberIds.add(currentUserId);
        List<ChatMember> members = memberIds.stream()
                .map(id -> createMember(chat, id, id.equals(currentUserId) ? MemberRole.ADMIN : MemberRole.MEMBER))
                .toList();
        chatMemberRepository.saveAll(members);
        chat.setMembers(members);
        return toDto(chat, currentUserId);
    }

    @Override
    public List<ChatDto> getChats(UUID currentUserId) {
        return chatRepository.findAllByMember(currentUserId).stream()
                .map(chat -> toDto(chat, currentUserId))
                .collect(Collectors.toList());
    }

    @Override
    public ChatDto getChat(UUID currentUserId, UUID chatId) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new NotFoundException("Chat not found"));
        ensureMember(chatId, currentUserId);
        return toDto(chat, currentUserId);
    }

    @Override
    public ChatDto addMembers(UUID currentUserId, UUID chatId, MemberModificationRequest request) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new NotFoundException("Chat not found"));
        ensureAdmin(chatId, currentUserId);
        List<ChatMember> newMembers = request.getMemberIds().stream()
                .map(id -> createMember(chat, id, MemberRole.MEMBER))
                .toList();
        chatMemberRepository.saveAll(newMembers);
        chat.getMembers().addAll(newMembers);
        return toDto(chat, currentUserId);
    }

    @Override
    public ChatDto removeMember(UUID currentUserId, UUID chatId, UUID memberId) {
        ensureAdmin(chatId, currentUserId);
        chatMemberRepository.deleteByChatIdAndUserId(chatId, memberId);
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new NotFoundException("Chat not found"));
        chat.setMembers(chatMemberRepository.findByChat(chat));
        return toDto(chat, currentUserId);
    }

    private void ensureMember(UUID chatId, UUID userId) {
        chatMemberRepository.findByChatIdAndUserId(chatId, userId)
                .orElseThrow(() -> new ForbiddenException("User is not a member of this chat"));
    }

    private void ensureAdmin(UUID chatId, UUID userId) {
        ChatMember member = chatMemberRepository.findByChatIdAndUserId(chatId, userId)
                .orElseThrow(() -> new ForbiddenException("User is not a member of this chat"));
        if (member.getRole() != MemberRole.ADMIN) {
            throw new ForbiddenException("Only chat admins can perform this action");
        }
    }

    private ChatMember createMember(Chat chat, UUID userId, MemberRole role) {
        ChatMember member = new ChatMember();
        member.setId(UUID.randomUUID());
        member.setChat(chat);
        member.setUserId(userId);
        member.setRole(role);
        member.setJoinedAt(Instant.now());
        return member;
    }

    private ChatDto toDto(Chat chat, UUID currentUserId) {
        MessageDto lastMessageDto = messageRepository.findTop1ByChatIdOrderByCreatedAtDesc(chat.getId())
                .map(this::toDto)
                .orElse(null);
        List<ChatMember> members = chat.getMembers().isEmpty() ? chatMemberRepository.findByChat(chat) : chat.getMembers();
        return ChatDto.builder()
                .id(chat.getId())
                .type(chat.getType())
                .title(chat.getTitle())
                .createdBy(chat.getCreatedBy())
                .createdAt(chat.getCreatedAt())
                .updatedAt(chat.getUpdatedAt())
                .members(members.stream().map(this::toDto).toList())
                .lastMessage(lastMessageDto)
                .build();
    }

    private ChatMemberDto toDto(ChatMember member) {
        return ChatMemberDto.builder()
                .id(member.getId())
                .userId(member.getUserId())
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .muted(member.isMuted())
                .build();
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
