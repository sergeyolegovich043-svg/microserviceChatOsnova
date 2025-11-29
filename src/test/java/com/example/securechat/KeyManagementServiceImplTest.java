package com.example.securechat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.securechat.domain.dto.KeyBundleDto;
import com.example.securechat.domain.dto.KeyBundleRequest;
import com.example.securechat.domain.model.UserPublicKeyBundle;
import com.example.securechat.domain.repository.UserPublicKeyBundleRepository;
import com.example.securechat.domain.service.impl.KeyManagementServiceImpl;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class KeyManagementServiceImplTest {

    private KeyManagementServiceImpl service;
    @Mock
    private UserPublicKeyBundleRepository repository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new KeyManagementServiceImpl(repository);
    }

    @Test
    void upsertsBundle() {
        UUID userId = UUID.randomUUID();
        KeyBundleRequest request = buildRequest();
        when(repository.findByUserIdAndDeviceId(userId, request.getDeviceId())).thenReturn(Optional.empty());
        KeyBundleDto dto = service.upsertBundle(userId, request);
        assertThat(dto.getUserId()).isEqualTo(userId);
        assertThat(dto.getOneTimePreKeysPublic()).hasSize(2);
    }

    @Test
    void readsBundles() {
        UUID targetUser = UUID.randomUUID();
        UserPublicKeyBundle bundle = new UserPublicKeyBundle();
        bundle.setId(UUID.randomUUID());
        bundle.setUserId(targetUser);
        bundle.setDeviceId(UUID.randomUUID());
        bundle.setIdentityKeyPublic("id");
        bundle.setSignedPreKeyPublic("spk");
        bundle.setOneTimePreKeysPublic("a,b");
        when(repository.findByUserId(targetUser)).thenReturn(List.of(bundle));
        List<KeyBundleDto> dtos = service.getBundles(UUID.randomUUID(), targetUser);
        assertThat(dtos).hasSize(1);
    }

    private KeyBundleRequest buildRequest() {
        KeyBundleRequest request = new KeyBundleRequest();
        request.setDeviceId(UUID.randomUUID());
        request.setIdentityKeyPublic("idKey");
        request.setSignedPreKeyPublic("spk");
        request.setOneTimePreKeysPublic(List.of("a", "b"));
        return request;
    }
}
