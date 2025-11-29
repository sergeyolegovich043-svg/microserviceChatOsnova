package com.example.securechat.domain.service.impl;

import com.example.securechat.domain.dto.KeyBundleDto;
import com.example.securechat.domain.dto.KeyBundleRequest;
import com.example.securechat.domain.model.UserPublicKeyBundle;
import com.example.securechat.domain.repository.UserPublicKeyBundleRepository;
import com.example.securechat.domain.service.KeyManagementService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class KeyManagementServiceImpl implements KeyManagementService {

    private final UserPublicKeyBundleRepository repository;

    public KeyManagementServiceImpl(UserPublicKeyBundleRepository repository) {
        this.repository = repository;
    }

    @Override
    public KeyBundleDto upsertBundle(UUID currentUserId, KeyBundleRequest request) {
        UserPublicKeyBundle bundle = repository.findByUserIdAndDeviceId(currentUserId, request.getDeviceId())
                .orElseGet(UserPublicKeyBundle::new);
        if (bundle.getId() == null) {
            bundle.setId(UUID.randomUUID());
        }
        bundle.setUserId(currentUserId);
        bundle.setDeviceId(request.getDeviceId());
        bundle.setIdentityKeyPublic(request.getIdentityKeyPublic());
        bundle.setSignedPreKeyPublic(request.getSignedPreKeyPublic());
        bundle.setOneTimePreKeysPublic(String.join(",", request.getOneTimePreKeysPublic()));
        bundle.setUpdatedAt(Instant.now());
        repository.save(bundle);
        return toDto(bundle);
    }

    @Override
    public List<KeyBundleDto> getBundles(UUID requestingUserId, UUID targetUserId) {
        // Authorization policy could enforce sharing restrictions. Currently any authenticated user can fetch bundles to initiate sessions.
        List<UserPublicKeyBundle> bundles = repository.findByUserId(targetUserId);
        return bundles.stream().map(this::toDto).toList();
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
}
