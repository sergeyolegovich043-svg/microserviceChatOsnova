package com.example.securechat.web.controller;

import com.example.securechat.domain.dto.KeyBundleDto;
import com.example.securechat.domain.dto.KeyBundleRequest;
import com.example.securechat.domain.service.KeyManagementService;
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
@RequestMapping("/api/v1/keys")
public class KeyController {

    private final KeyManagementService keyManagementService;

    public KeyController(KeyManagementService keyManagementService) {
        this.keyManagementService = keyManagementService;
    }

    @PostMapping("/bundle")
    public ResponseEntity<KeyBundleDto> uploadBundle(@Valid @RequestBody KeyBundleRequest request) {
        return ResponseEntity.ok(keyManagementService.upsertBundle(CurrentUser.getUserId(), request));
    }

    @GetMapping("/bundle/{userId}")
    public ResponseEntity<List<KeyBundleDto>> getBundles(@PathVariable UUID userId) {
        return ResponseEntity.ok(keyManagementService.getBundles(CurrentUser.getUserId(), userId));
    }
}
