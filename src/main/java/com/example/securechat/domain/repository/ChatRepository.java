package com.example.securechat.domain.repository;

import com.example.securechat.domain.model.Chat;
import com.example.securechat.domain.model.ChatType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRepository extends JpaRepository<Chat, UUID> {

    @Query("select c from Chat c join c.members m1 join c.members m2 " +
            "where c.type = :type and m1.userId = :user1 and m2.userId = :user2")
    Optional<Chat> findPrivateChatBetween(@Param("type") ChatType type,
                                          @Param("user1") UUID user1,
                                          @Param("user2") UUID user2);

    @Query("select distinct c from Chat c join c.members m where m.userId = :userId")
    List<Chat> findAllByMember(@Param("userId") UUID userId);
}
