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

import com.webank.wedpr.components.loadbalancer.LoadBalancer;
import com.webank.wedpr.components.loadbalancer.impl.EntryPointConfigLoader;
import com.webank.wedpr.components.loadbalancer.impl.LoadBalancerImpl;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class LoadBalanceConfig {
    @Bean(name = "loadBalancer")
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    // @ConditionalOnProperty(value = "wedpr.service.debugMode", havingValue = "true")
    @ConditionalOnMissingBean
    public LoadBalancer debugModeloadBalancer() {
        return new LoadBalancerImpl(new EntryPointConfigLoader());
    }
}
