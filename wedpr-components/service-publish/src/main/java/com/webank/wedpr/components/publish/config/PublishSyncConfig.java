package com.webank.wedpr.components.publish.config;

import com.webank.wedpr.components.publish.sync.PublishSyncer;
import com.webank.wedpr.components.publish.sync.PublishSyncerCommitHandler;
import com.webank.wedpr.components.publish.sync.api.PublishSyncerApi;
import com.webank.wedpr.components.sync.ResourceSyncer;
import com.webank.wedpr.components.sync.core.ResourceActionRecorderBuilder;
import com.webank.wedpr.core.config.WeDPRCommonConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zachma
 * @date 2024/8/28
 */
@Configuration
public class PublishSyncConfig {
    private static final Logger logger = LoggerFactory.getLogger(PublishSyncConfig.class);

    @Autowired
    @Qualifier("resourceSyncer")
    private ResourceSyncer resourceSyncer;

    @Autowired
    @Qualifier("publishSyncerCommitHandler")
    PublishSyncerCommitHandler publishSyncerCommitHandler;

    @Bean(name = "publishSyncer")
    public PublishSyncerApi newPublishSyncer() {

        PublishSyncer publishSyncer = new PublishSyncer();
        String agency = WeDPRCommonConfig.getAgency();
        logger.info(" => create publish syncer, agency: {}", agency);

        String resourceType = ResourceSyncer.ResourceType.Publish.getType();
        ResourceActionRecorderBuilder resourceBuilder =
                new ResourceActionRecorderBuilder(agency, resourceType);
        publishSyncer.setResourceSyncer(resourceSyncer);
        publishSyncer.setResourceBuilder(resourceBuilder);
        resourceSyncer.registerCommitHandler(resourceType, publishSyncerCommitHandler);
        return publishSyncer;
    }
}
