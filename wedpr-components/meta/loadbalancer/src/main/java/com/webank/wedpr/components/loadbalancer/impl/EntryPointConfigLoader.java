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

package com.webank.wedpr.components.loadbalancer.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.wedpr.common.config.WeDPRConfig;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.loadbalancer.EntryPointFetcher;
import com.webank.wedpr.components.loadbalancer.EntryPointInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntryPointConfigLoader implements EntryPointFetcher {
    private static final Logger logger = LoggerFactory.getLogger(EntryPointConfigLoader.class);
    private Map<String, List<EntryPointInfo>> alivedEntryPoints = new HashMap<>();

    @SneakyThrows(Exception.class)
    public EntryPointConfigLoader() {
        String entryPointsInfo = WeDPRConfig.apply("wedpr.service.entrypoints", null);
        if (StringUtils.isBlank(entryPointsInfo)) {
            throw new WeDPRException("Must configure the wedpr.service.entrypoints");
        }
        logger.info("load entryPointsInfo: {}", entryPointsInfo);
        List<ConfiguratedEntryPoints> entryPointsList =
                ObjectMapperFactory.getObjectMapper()
                        .readValue(
                                entryPointsInfo,
                                new TypeReference<List<ConfiguratedEntryPoints>>() {});
        for (ConfiguratedEntryPoints entryPoint : entryPointsList) {
            logger.info(
                    "add entrypoint info for service: {}, entryPointsSize: {}",
                    entryPoint.getServiceName(),
                    entryPoint.getEntryPoints().size());
            alivedEntryPoints.put(
                    entryPoint.getServiceName().toLowerCase(),
                    EntryPointInfo.toEntryPointInfo(
                            entryPoint.getServiceName(), entryPoint.getEntryPoints()));
        }
    }

    @Override
    public List<EntryPointInfo> getAliveEntryPoints(String serviceName) {
        String lowerCaseServiceName = serviceName.toLowerCase();
        if (alivedEntryPoints.containsKey(lowerCaseServiceName)) {
            return alivedEntryPoints.get(lowerCaseServiceName);
        }
        return null;
    }
}
