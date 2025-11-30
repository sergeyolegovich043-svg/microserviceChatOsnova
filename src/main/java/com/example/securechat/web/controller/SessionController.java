package com.example.securechat.web.controller;

import com.example.securechat.domain.dto.KeyBundleDto;
import com.example.securechat.domain.dto.SessionDto;
import com.example.securechat.domain.dto.SessionInitRequest;
import com.example.securechat.domain.service.SessionService;
import com.example.securechat.security.CurrentUser;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chats/{chatId}/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public ResponseEntity<SessionDto> startSession(@PathVariable UUID chatId,
            @Valid @RequestBody SessionInitRequest request) {
        return ResponseEntity.ok(sessionService.startSession(CurrentUser.getUserId(), chatId, request));
    }

    @GetMapping
    public ResponseEntity<List<SessionDto>> getSessions(@PathVariable UUID chatId) {
        return ResponseEntity.ok(sessionService.getSessions(CurrentUser.getUserId(), chatId));
    }

    @GetMapping("/bundles")
    public ResponseEntity<List<KeyBundleDto>> getEncryptionBundles(@PathVariable UUID chatId) {
        return ResponseEntity.ok(sessionService.getChatEncryptionBundles(CurrentUser.getUserId(), chatId));
    }
}
