package com.example.securechat.web.controller;

import com.example.securechat.domain.dto.MediaPresignRequest;
import com.example.securechat.domain.dto.MediaPresignResponse;
import com.example.securechat.domain.service.ObjectStorageService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/media")
public class MediaController {

    private final ObjectStorageService objectStorageService;

    public MediaController(ObjectStorageService objectStorageService) {
        this.objectStorageService = objectStorageService;
    }

    @PostMapping("/presign")
    public ResponseEntity<MediaPresignResponse> presign(@Valid @RequestBody MediaPresignRequest request) {
        return ResponseEntity.ok(objectStorageService.createPresignedUpload(request));
    }
}
