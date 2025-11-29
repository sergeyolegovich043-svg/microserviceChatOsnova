package com.example.securechat.config;

import com.example.securechat.domain.service.ObjectStorageService;
import com.example.securechat.domain.service.impl.MockObjectStorageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectStorageConfig {

    @Bean
    public ObjectStorageService objectStorageService() {
        return new MockObjectStorageService();
    }
}
