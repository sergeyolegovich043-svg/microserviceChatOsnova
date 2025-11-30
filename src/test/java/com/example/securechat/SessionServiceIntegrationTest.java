package com.example.securechat;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.securechat.domain.dto.KeyBundleDto;
import com.example.securechat.domain.dto.SessionDto;
import com.example.securechat.domain.dto.SessionInitRequest;
import com.example.securechat.domain.model.Chat;
import com.example.securechat.domain.model.ChatMember;
import com.example.securechat.domain.model.ChatType;
import com.example.securechat.domain.model.MemberRole;
import com.example.securechat.domain.model.UserPublicKeyBundle;
import com.example.securechat.domain.repository.ChatMemberRepository;
import com.example.securechat.domain.repository.ChatRepository;
import com.example.securechat.domain.repository.SessionMetadataRepository;
import com.example.securechat.domain.repository.UserPublicKeyBundleRepository;
import com.example.securechat.domain.service.SessionService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class SessionServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private SessionService sessionService;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ChatMemberRepository chatMemberRepository;

    @Autowired
    private UserPublicKeyBundleRepository keyBundleRepository;

    @Autowired
    private SessionMetadataRepository sessionMetadataRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private UUID userId;
    private UUID deviceId;
    private UUID chatId;

    @BeforeEach
    void setUp() {
        sessionMetadataRepository.deleteAll();
        keyBundleRepository.deleteAll();
        chatMemberRepository.deleteAll();
        chatRepository.deleteAll();
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();

        userId = UUID.randomUUID();
        deviceId = UUID.randomUUID();
        chatId = UUID.randomUUID();

        Chat chat = new Chat();
        chat.setId(chatId);
        chat.setType(ChatType.PRIVATE);
        chat.setCreatedBy(userId);
        chat.setCreatedAt(Instant.now());
        chat.setUpdatedAt(Instant.now());
        chatRepository.save(chat);

        ChatMember member = new ChatMember();
        member.setId(UUID.randomUUID());
        member.setChat(chat);
        member.setUserId(userId);
        member.setRole(MemberRole.ADMIN);
        member.setJoinedAt(Instant.now());
        chatMemberRepository.save(member);

        UserPublicKeyBundle bundle = new UserPublicKeyBundle();
        bundle.setId(UUID.randomUUID());
        bundle.setUserId(userId);
        bundle.setDeviceId(deviceId);
        bundle.setIdentityKeyPublic("idKey");
        bundle.setSignedPreKeyPublic("spk");
        bundle.setOneTimePreKeysPublic("otp1,otp2");
        bundle.setUpdatedAt(Instant.now());
        keyBundleRepository.save(bundle);
    }

    @Test
    void startsSessionAndCachesBundle() {
        SessionInitRequest request = new SessionInitRequest();
        request.setDeviceId(deviceId);
        request.setClientSessionId("client-session-1");

        SessionDto session = sessionService.startSession(userId, chatId, request);

        assertThat(sessionMetadataRepository.findByChatId(chatId)).hasSize(1);
        assertThat(session.getKeyBundle()).isNotNull();

        Object cached = redisTemplate.opsForValue().get("session:bundle:" + session.getId());
        assertThat(cached).isInstanceOf(KeyBundleDto.class);
    }

    @Test
    void returnsEncryptionBundlesForChatMembers() {
        // prepare second member with own bundle
        UUID peerId = UUID.randomUUID();
        ChatMember peerMember = new ChatMember();
        peerMember.setId(UUID.randomUUID());
        peerMember.setChat(chatRepository.findById(chatId).orElseThrow());
        peerMember.setUserId(peerId);
        peerMember.setRole(MemberRole.MEMBER);
        peerMember.setJoinedAt(Instant.now());
        chatMemberRepository.save(peerMember);

        UserPublicKeyBundle peerBundle = new UserPublicKeyBundle();
        peerBundle.setId(UUID.randomUUID());
        peerBundle.setUserId(peerId);
        peerBundle.setDeviceId(UUID.randomUUID());
        peerBundle.setIdentityKeyPublic("peer-id");
        peerBundle.setSignedPreKeyPublic("peer-spk");
        peerBundle.setOneTimePreKeysPublic("x,y");
        peerBundle.setUpdatedAt(Instant.now());
        keyBundleRepository.save(peerBundle);

        List<KeyBundleDto> bundles = sessionService.getChatEncryptionBundles(userId, chatId);

        assertThat(bundles).extracting(KeyBundleDto::getUserId).contains(userId, peerId);
    }
}
