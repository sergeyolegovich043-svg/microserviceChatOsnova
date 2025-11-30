package com.example.securechat.domain.service;

import com.example.securechat.domain.dto.KeyBundleDto;
import com.example.securechat.domain.dto.SessionDto;
import com.example.securechat.domain.dto.SessionInitRequest;
import java.util.List;
import java.util.UUID;

public interface SessionService {

    SessionDto startSession(UUID userId, UUID chatId, SessionInitRequest request);

    List<SessionDto> getSessions(UUID userId, UUID chatId);

    List<KeyBundleDto> getChatEncryptionBundles(UUID userId, UUID chatId);
}
