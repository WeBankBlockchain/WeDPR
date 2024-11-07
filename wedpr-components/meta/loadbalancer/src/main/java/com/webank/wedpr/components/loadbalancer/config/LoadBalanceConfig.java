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

package com.webank.wedpr.components.loadbalancer.config;

import com.webank.wedpr.common.config.WeDPRConfig;
import com.webank.wedpr.components.loadbalancer.LoadBalancer;
import com.webank.wedpr.components.loadbalancer.impl.EntryPointConfigLoader;
import com.webank.wedpr.components.loadbalancer.impl.EntryPointFetcherImpl;
import com.webank.wedpr.components.loadbalancer.impl.LoadBalancerImpl;
import com.webank.wedpr.sdk.jni.transport.WeDPRTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class LoadBalanceConfig {
    private static final Logger logger = LoggerFactory.getLogger(LoadBalanceConfig.class);
    @Autowired private WeDPRTransport weDPRTransport;

    private static boolean DEBUG_MODE = WeDPRConfig.apply("wedpr.service.debugMode", false);

    @Bean(name = "loadBalancer")
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    @ConditionalOnMissingBean
    public LoadBalancer loadBalancer() {
        if (DEBUG_MODE) {
            logger.info("Create debug mode LoadBalancerImpl, load service config from config file");
            return new LoadBalancerImpl(new EntryPointConfigLoader());
        }
        logger.info("Create LoadBalancerImpl, fetch the alive node from the gateway");
        return new LoadBalancerImpl(new EntryPointFetcherImpl(weDPRTransport));
    }
}
