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

import com.webank.wedpr.components.scheduler.local.executor.impl.model.FileMetaBuilder;
import com.webank.wedpr.components.storage.builder.StoragePathBuilder;
import com.webank.wedpr.components.storage.config.HdfsStorageConfig;
import com.webank.wedpr.components.storage.config.LocalStorageConfig;
import com.webank.wedpr.components.sync.config.ResourceSyncerConfig;
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
@AutoConfigureAfter(ResourceSyncerConfig.class)
public class FileMetaBuilderConfig {
    private static final Logger logger = LoggerFactory.getLogger(FileMetaBuilderConfig.class);
    @Autowired private LocalStorageConfig localStorageConfig;
    @Autowired private HdfsStorageConfig hdfsConfig;

    @Bean(name = "fileMetaBuilder")
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    @ConditionalOnMissingBean
    public FileMetaBuilder fileMetaBuilder() throws Exception {

        return new FileMetaBuilder(new StoragePathBuilder(hdfsConfig, localStorageConfig));
    }
}
