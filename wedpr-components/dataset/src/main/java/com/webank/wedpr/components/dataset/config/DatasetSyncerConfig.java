package com.webank.wedpr.components.dataset.config;

import com.webank.wedpr.common.config.WeDPRCommonConfig;
import com.webank.wedpr.components.dataset.sync.DatasetSyncer;
import com.webank.wedpr.components.dataset.sync.DatasetSyncerCommitHandler;
import com.webank.wedpr.components.dataset.sync.api.DatasetSyncerApi;
import com.webank.wedpr.components.db.mapper.dataset.mapper.wapper.DatasetTransactionalWrapper;
import com.webank.wedpr.components.sync.ResourceSyncer;
import com.webank.wedpr.components.sync.ResourceSyncer.ResourceType;
import com.webank.wedpr.components.sync.core.ResourceActionRecorderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatasetSyncerConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatasetSyncerConfig.class);

    @Autowired
    @Qualifier("resourceSyncer")
    private ResourceSyncer resourceSyncer;

    @Autowired DatasetTransactionalWrapper transactionalWrapper;

    @Autowired
    @Qualifier("datasetSyncerCommitHandler")
    DatasetSyncerCommitHandler datasetSyncerCommitHandler;

    @Bean(name = "datasetSyncer")
    public DatasetSyncerApi createDatasetSyncer() {

        DatasetSyncer datasetSyncer = new DatasetSyncer();

        String agency = WeDPRCommonConfig.getAgency();
        logger.info(" => create dataset syncer, agency: {}", agency);

        String resourceType = ResourceType.Dataset.getType();
        ResourceActionRecorderBuilder resourceBuilder =
                new ResourceActionRecorderBuilder(agency, resourceType);
        datasetSyncer.setAgency(agency);
        datasetSyncer.setResourceSyncer(resourceSyncer);
        datasetSyncer.setResourceBuilder(resourceBuilder);
        datasetSyncer.setTransactionalWrapper(transactionalWrapper);

        resourceSyncer.registerCommitHandler(resourceType, datasetSyncerCommitHandler);

        return datasetSyncer;
    }
}
