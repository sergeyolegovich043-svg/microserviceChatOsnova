package com.example.securechat.domain.repository;

import com.example.securechat.domain.model.MessageReceipt;
import com.example.securechat.domain.model.ReceiptStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageReceiptRepository extends JpaRepository<MessageReceipt, UUID> {
    Optional<MessageReceipt> findByMessageIdAndUserId(UUID messageId, UUID userId);

    long countByMessageChatIdAndUserIdAndStatus(UUID chatId, UUID userId, ReceiptStatus status);
}
