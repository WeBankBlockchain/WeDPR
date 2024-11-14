package com.webank.wedpr.components.dataset.config;

import com.webank.wedpr.components.dataset.service.DatasetStatusUpdater;
import com.webank.wedpr.components.db.mapper.dataset.mapper.DatasetMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatasetStatusUpdaterConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatasetStatusUpdaterConfig.class);

    @Autowired DatasetMapper datasetMapper;

    @Autowired DatasetConfig datasetConfig;

    @Bean(name = "datasetStatusUpdater")
    public DatasetStatusUpdater newDatasetStatusUpdaterConfig() {
        logger.info("## Create DatasetStatusUpdater");
        DatasetStatusUpdater datasetStatusUpdater =
                new DatasetStatusUpdater(datasetConfig, datasetMapper);
        datasetStatusUpdater.start();
        return datasetStatusUpdater;
    }
}
