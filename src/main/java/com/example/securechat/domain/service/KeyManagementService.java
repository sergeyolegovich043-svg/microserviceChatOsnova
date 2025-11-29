package com.example.securechat.domain.service;

import com.example.securechat.domain.dto.KeyBundleDto;
import com.example.securechat.domain.dto.KeyBundleRequest;
import java.util.List;
import java.util.UUID;

public interface KeyManagementService {
    KeyBundleDto upsertBundle(UUID currentUserId, KeyBundleRequest request);

    List<KeyBundleDto> getBundles(UUID requestingUserId, UUID targetUserId);
}
