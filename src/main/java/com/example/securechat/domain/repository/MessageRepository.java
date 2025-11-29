package com.example.securechat.domain.repository;

import com.example.securechat.domain.model.Message;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findByChatIdOrderByCreatedAtDesc(UUID chatId, Pageable pageable);

    @Query("select m from Message m where m.chat.id = :chatId and m.createdAt < (select createdAt from Message where id = :beforeId) order by m.createdAt desc")
    List<Message> findByChatIdAndBeforeMessageId(UUID chatId, UUID beforeId, Pageable pageable);

    Optional<Message> findTop1ByChatIdOrderByCreatedAtDesc(UUID chatId);
}
