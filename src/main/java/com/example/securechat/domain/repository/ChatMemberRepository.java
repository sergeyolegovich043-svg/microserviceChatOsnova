package com.example.securechat.domain.repository;

import com.example.securechat.domain.model.Chat;
import com.example.securechat.domain.model.ChatMember;
import com.example.securechat.domain.model.MemberRole;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatMemberRepository extends JpaRepository<ChatMember, UUID> {
    List<ChatMember> findByChat(Chat chat);

    Optional<ChatMember> findByChatIdAndUserId(UUID chatId, UUID userId);

    @Query("select cm from ChatMember cm where cm.chat.id = :chatId and cm.role = 'ADMIN'")
    List<ChatMember> findAdmins(UUID chatId);

    void deleteByChatIdAndUserId(UUID chatId, UUID userId);
}
