package com.webank.wedpr.components.storage.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
@ConfigurationProperties(prefix = "wedpr.storage.local")
@Data
public class LocalStorageConfig {
    private String baseDir;
    public String getStorageAbsPath(String filePath) {
        return baseDir + File.separator + filePath;
    }
}
