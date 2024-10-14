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

package com.webank.wedpr.components.authorization.config;

import com.webank.wedpr.common.config.WeDPRCommonConfig;
import com.webank.wedpr.common.utils.ThreadPoolService;
import com.webank.wedpr.components.authorization.dao.AuthMapperWrapper;
import com.webank.wedpr.components.authorization.service.impl.AuthSyncer;
import com.webank.wedpr.components.sync.ResourceSyncer;
import com.webank.wedpr.components.sync.config.ResourceSyncerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@AutoConfigureAfter(ResourceSyncerConfig.class)
public class AuthSyncConfig {
    @Autowired private ResourceSyncer resourceSyncer;
    @Autowired private ThreadPoolService syncWorkerThreadPool;
    @Autowired private AuthMapperWrapper authMapperWrapper;

    // Note: the authSyncer should be inited before start the ResourceSyncer
    @Bean(name = "authSyncer")
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    @ConditionalOnMissingBean
    public AuthSyncer authSyncer() {
        return new AuthSyncer(
                WeDPRCommonConfig.getAgency(),
                ResourceSyncer.ResourceType.Authorization.getType(),
                syncWorkerThreadPool,
                resourceSyncer,
                authMapperWrapper);
    }
}
