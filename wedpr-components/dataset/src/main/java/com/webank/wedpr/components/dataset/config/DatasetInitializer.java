package com.webank.wedpr.components.dataset.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatasetInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DatasetInitializer.class);

    @Autowired DatasetConfig datasetConfig;

    @Bean
    public void createLargeFileDataDir() throws IOException {
        String largeFileDataDir = datasetConfig.getLargeFileDataDir();

        File file = new File(largeFileDataDir);
        if (!file.exists()) {
            Files.createDirectories(file.toPath());
            logger.info(" => create large file data dir, largeFileDataDir: {}", largeFileDataDir);
        } else {
            logger.info(
                    " => large file data dir has been exist, largeFileDataDir: {}",
                    largeFileDataDir);
        }
    }
}
