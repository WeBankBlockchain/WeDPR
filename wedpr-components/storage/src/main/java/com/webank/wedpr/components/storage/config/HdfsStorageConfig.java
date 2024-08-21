package com.webank.wedpr.components.storage.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@ConfigurationProperties(prefix = "wedpr.storage.hdfs")
@Configuration
@Data
public class HdfsStorageConfig {
    private String user = "wedpr";
    private String url;
    private String baseDir = "/user";

    public String getAbsPathInHdfs(String path) {
        return getBaseDir()
                + File.separator
                + getUser()
                + File.separator + path;
    }
}
