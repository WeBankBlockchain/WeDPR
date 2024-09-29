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

package com.webank.wedpr.components.scheduler.local.config;

import com.webank.wedpr.components.project.JobChecker;
import com.webank.wedpr.components.project.dao.ProjectMapperWrapper;
import com.webank.wedpr.components.scheduler.SchedulerBuilder;
import com.webank.wedpr.components.scheduler.local.core.SchedulerTaskImpl;
import com.webank.wedpr.components.scheduler.local.executor.impl.model.FileMetaBuilder;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import com.webank.wedpr.components.storage.builder.StoragePathBuilder;
import com.webank.wedpr.components.storage.config.HdfsStorageConfig;
import com.webank.wedpr.components.storage.config.LocalStorageConfig;
import com.webank.wedpr.components.sync.ResourceSyncer;
import com.webank.wedpr.components.sync.config.ResourceSyncerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@AutoConfigureAfter({ResourceSyncerConfig.class, JobCheckerConfig.class})
public class SchedulerLoader {
    private static final Logger logger = LoggerFactory.getLogger(SchedulerLoader.class);
    @Autowired private ProjectMapperWrapper projectMapperWrapper;

    @Autowired private LocalStorageConfig localStorageConfig;
    @Autowired private HdfsStorageConfig hdfsConfig;

    @Qualifier("fileStorage")
    @Autowired
    private FileStorageInterface storage;

    @Autowired private ResourceSyncer resourceSyncer;
    @Autowired private JobChecker jobChecker;

    @Bean(name = "schedulerTaskImpl")
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    @ConditionalOnMissingBean
    public SchedulerTaskImpl schedulerTaskImpl() throws Exception {
        SchedulerTaskImpl schedulerTask =
                SchedulerBuilder.buildSchedulerTask(
                        projectMapperWrapper,
                        storage,
                        resourceSyncer,
                        new FileMetaBuilder(new StoragePathBuilder(hdfsConfig, localStorageConfig)),
                        jobChecker);
        schedulerTask.start();
        return schedulerTask;
    }
}
