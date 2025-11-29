package com.example.securechat.domain.service.impl;

import com.example.securechat.domain.dto.MediaPresignRequest;
import com.example.securechat.domain.dto.MediaPresignResponse;
import com.example.securechat.domain.service.ObjectStorageService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

public class MockObjectStorageService implements ObjectStorageService {

    @Override
    public MediaPresignResponse createPresignedUpload(MediaPresignRequest request) {
        // In production, integrate with AWS S3/GCS/etc. to generate signed URLs.
        String objectKey = UUID.randomUUID() + "-" + URLEncoder.encode(request.getFileName(), StandardCharsets.UTF_8);
        String uploadUrl = "https://object-storage.example.com/upload/" + objectKey + "?signature=dummy";
        String finalUrl = "https://object-storage.example.com/objects/" + objectKey + "?ts=" + Instant.now().toEpochMilli();
        return new MediaPresignResponse(uploadUrl, finalUrl);
    }
}
