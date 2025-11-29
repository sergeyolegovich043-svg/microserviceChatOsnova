package com.example.securechat.domain.repository;

import com.example.securechat.domain.model.UserPublicKeyBundle;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPublicKeyBundleRepository extends JpaRepository<UserPublicKeyBundle, UUID> {
    Optional<UserPublicKeyBundle> findByUserIdAndDeviceId(UUID userId, UUID deviceId);

    List<UserPublicKeyBundle> findByUserId(UUID userId);
}
