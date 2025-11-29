package com.example.securechat.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MediaPresignResponse {
    private String uploadUrl;
    private String finalUrl;
}
