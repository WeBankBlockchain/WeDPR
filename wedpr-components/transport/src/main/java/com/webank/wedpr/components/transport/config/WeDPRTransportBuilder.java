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

package com.webank.wedpr.components.transport.config;

import com.webank.wedpr.sdk.jni.transport.TransportConfig;
import com.webank.wedpr.sdk.jni.transport.WeDPRTransport;
import com.webank.wedpr.sdk.jni.transport.impl.TransportImpl;
import com.webank.wedpr.sdk.jni.transport.model.TransportEndPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class WeDPRTransportBuilder {
    private static final Logger logger = LoggerFactory.getLogger(WeDPRTransportBuilder.class);

    @Bean(name = "weDPRTransport")
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    @ConditionalOnMissingBean
    public WeDPRTransport weDPRTransport() {
        // create the transportConfig
        TransportConfig config =
                new TransportConfig(
                        WeDPRTransportConfig.getThreadPoolSize(), WeDPRTransportConfig.getNODEID());
        config.setGatewayTargets(WeDPRTransportConfig.getGatewayTargets());
        TransportEndPoint selfEndPoint =
                new TransportEndPoint(
                        WeDPRTransportConfig.getHostIp(),
                        WeDPRTransportConfig.getListenIp(),
                        WeDPRTransportConfig.getListenPort());
        config.setSelfEndPoint(selfEndPoint);
        logger.info("Begin to create wedpr transport with config: {}", config.toString());
        // build transport through config
        WeDPRTransport transport = TransportImpl.build(config);
        transport.start();
        logger.info("Create weDPRTransport success, config: {}", config.toString());
        return transport;
    }
}
