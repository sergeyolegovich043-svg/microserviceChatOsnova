package com.example.securechat.domain.service.impl;

import com.example.securechat.domain.dto.KeyBundleDto;
import com.example.securechat.domain.dto.SessionDto;
import com.example.securechat.domain.dto.SessionInitRequest;
import com.example.securechat.domain.model.Chat;
import com.example.securechat.domain.model.ChatMember;
import com.example.securechat.domain.model.UserPublicKeyBundle;
import com.example.securechat.domain.repository.ChatMemberRepository;
import com.example.securechat.domain.repository.ChatRepository;
import com.example.securechat.domain.repository.SessionMetadataRepository;
import com.example.securechat.domain.repository.UserPublicKeyBundleRepository;
import com.example.securechat.domain.service.SessionService;
import com.example.securechat.exception.BadRequestException;
import com.example.securechat.exception.ForbiddenException;
import com.example.securechat.exception.NotFoundException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SessionServiceImpl implements SessionService {

    private static final String SESSION_BUNDLE_CACHE_PREFIX = "session:bundle:";
    private static final Duration SESSION_CACHE_TTL = Duration.ofHours(24);

    private final SessionMetadataRepository sessionMetadataRepository;
    private final ChatRepository chatRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final UserPublicKeyBundleRepository keyBundleRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public SessionServiceImpl(SessionMetadataRepository sessionMetadataRepository, ChatRepository chatRepository,
            ChatMemberRepository chatMemberRepository, UserPublicKeyBundleRepository keyBundleRepository,
            RedisTemplate<String, Object> redisTemplate) {
        this.sessionMetadataRepository = sessionMetadataRepository;
        this.chatRepository = chatRepository;
        this.chatMemberRepository = chatMemberRepository;
        this.keyBundleRepository = keyBundleRepository;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public SessionDto startSession(UUID userId, UUID chatId, SessionInitRequest request) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new NotFoundException("Chat not found"));
        ensureMember(chatId, userId);
        UserPublicKeyBundle bundle = keyBundleRepository.findByUserIdAndDeviceId(userId, request.getDeviceId())
                .orElseThrow(() -> new BadRequestException("Key bundle not registered for device"));

        var metadata = new com.example.securechat.domain.model.SessionMetadata();
        metadata.setId(UUID.randomUUID());
        metadata.setChatId(chat.getId());
        metadata.setUserId(userId);
        metadata.setClientSessionId(request.getClientSessionId());
        metadata.setCreatedAt(Instant.now());
        sessionMetadataRepository.save(metadata);

        KeyBundleDto bundleDto = toDto(bundle);
        cacheBundle(metadata.getId(), bundleDto);
        return toDto(metadata, bundleDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDto> getSessions(UUID userId, UUID chatId) {
        ensureMember(chatId, userId);
        return sessionMetadataRepository.findByChatId(chatId).stream()
                .map(metadata -> toDto(metadata, resolveCachedBundle(metadata.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<KeyBundleDto> getChatEncryptionBundles(UUID userId, UUID chatId) {
        ensureMember(chatId, userId);
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new NotFoundException("Chat not found"));
        return chatMemberRepository.findByChat(chat).stream()
                .flatMap(member -> keyBundleRepository.findByUserId(member.getUserId()).stream())
                .map(this::toDto)
                .toList();
    }

    private void ensureMember(UUID chatId, UUID userId) {
        chatMemberRepository.findByChatIdAndUserId(chatId, userId)
                .orElseThrow(() -> new ForbiddenException("User is not a member of this chat"));
    }

    private void cacheBundle(UUID sessionId, KeyBundleDto bundle) {
        String key = SESSION_BUNDLE_CACHE_PREFIX + sessionId;
        redisTemplate.opsForValue().set(key, bundle, SESSION_CACHE_TTL);
    }

    private KeyBundleDto resolveCachedBundle(UUID sessionId) {
        String key = SESSION_BUNDLE_CACHE_PREFIX + sessionId;
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof KeyBundleDto dto) {
            return dto;
        }
        return null;
    }

    private KeyBundleDto toDto(UserPublicKeyBundle bundle) {
        return KeyBundleDto.builder()
                .id(bundle.getId())
                .userId(bundle.getUserId())
                .deviceId(bundle.getDeviceId())
                .identityKeyPublic(bundle.getIdentityKeyPublic())
                .signedPreKeyPublic(bundle.getSignedPreKeyPublic())
                .oneTimePreKeysPublic(List.of(bundle.getOneTimePreKeysPublic().split(",")))
                .updatedAt(bundle.getUpdatedAt())
                .build();
    }

    private SessionDto toDto(com.example.securechat.domain.model.SessionMetadata metadata, KeyBundleDto bundle) {
        return SessionDto.builder()
                .id(metadata.getId())
                .chatId(metadata.getChatId())
                .userId(metadata.getUserId())
                .clientSessionId(metadata.getClientSessionId())
                .createdAt(metadata.getCreatedAt())
                .keyBundle(bundle)
                .build();
    }
}
