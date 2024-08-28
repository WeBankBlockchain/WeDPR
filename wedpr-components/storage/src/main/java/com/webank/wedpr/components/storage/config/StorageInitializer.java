package com.webank.wedpr.components.storage.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageInitializer {

    private static final Logger logger = LoggerFactory.getLogger(StorageInitializer.class);

    @Autowired(required = false)
    private LocalStorageConfig localStorageConfig;

    @Autowired(required = false)
    private HdfsStorageConfig hdfsStorageConfig;

    @Bean
    public void initStorage() throws IOException {

        logger.info(
                " => initStorage localStorageConfig: {},  hdfsStorageConfig: {}",
                localStorageConfig,
                hdfsStorageConfig);

        if (localStorageConfig == null) {
            return;
        }

        String baseDir = localStorageConfig.getBaseDir();
        File file = new File(baseDir);
        if (!file.exists()) {
            Files.createDirectories(file.toPath());
            logger.info(" create local file storage dir, baseDir: {}", baseDir);
        } else {
            logger.info(" local file storage dir has been exist, baseDir: {}", baseDir);
        }
    }
}
