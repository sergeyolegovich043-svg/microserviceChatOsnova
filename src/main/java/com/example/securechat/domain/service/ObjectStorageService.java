package com.example.securechat.domain.service;

import com.example.securechat.domain.dto.MediaPresignRequest;
import com.example.securechat.domain.dto.MediaPresignResponse;

public interface ObjectStorageService {

    MediaPresignResponse createPresignedUpload(MediaPresignRequest request);
}
