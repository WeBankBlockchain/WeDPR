/*
 * Copyright 2017-2025  [webank-wedpr]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.webank.wedpr.components.sync.config;

import com.webank.wedpr.common.config.WeDPRConfig;
import com.webank.wedpr.common.utils.ThreadPoolService;
import com.webank.wedpr.components.leader.election.LeaderElection;
import com.webank.wedpr.components.leader.election.config.LeaderElectionConfig;
import com.webank.wedpr.components.meta.sys.config.WeDPRSysConfig;
import com.webank.wedpr.components.sync.ResourceSyncer;
import com.webank.wedpr.components.sync.dao.SyncStatusMapper;
import com.webank.wedpr.components.sync.dao.SyncStatusMapperWrapper;
import com.webank.wedpr.components.sync.impl.BlockChainResourceSyncImpl;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@AutoConfigureAfter(LeaderElectionConfig.class)
public class ResourceSyncerConfig {
    private static final Logger logger = LoggerFactory.getLogger(ResourceSyncerConfig.class);

    @Autowired private WeDPRSysConfig weDPRSysConfig;

    @Autowired private LeaderElection leaderElection;

    @Resource private SyncStatusMapper syncStatusMapper;

    @Bean(name = "resourceSyncer")
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    @ConditionalOnMissingBean
    public ResourceSyncer resourceSyncer() {
        logger.info("init resourceSyncer");
        Integer maxBlockingQueueSize =
                WeDPRConfig.apply("wedpr.thread.max.blocking.queue.size", 1000);
        return new BlockChainResourceSyncImpl(
                weDPRSysConfig,
                leaderElection,
                new SyncStatusMapperWrapper(syncStatusMapper),
                new ThreadPoolService("resourceSyncer", maxBlockingQueueSize));
    }

    @Bean(name = "syncWorkerThreadPool")
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    @ConditionalOnMissingBean
    public ThreadPoolService syncWorkerThreadPool() {
        return new ThreadPoolService(
                "syncWorker", WeDPRConfig.apply("wedpr.thread.max.blocking.queue.size", 1000));
    }
}
