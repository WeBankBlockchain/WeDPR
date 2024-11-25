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

import com.webank.wedpr.components.loadbalancer.EntryPointFetcher;
import com.webank.wedpr.components.loadbalancer.LoadBalancer;
import com.webank.wedpr.sdk.jni.transport.model.ServiceMeta;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadBalancerImpl implements LoadBalancer {
    private static final Logger logger = LoggerFactory.getLogger(LoadBalancerImpl.class);

    private final EntryPointFetcher entryPointFetcher;
    private final AtomicInteger lastIdx = new AtomicInteger(0);

    public LoadBalancerImpl(EntryPointFetcher entryPointFetcher) {
        this.entryPointFetcher = entryPointFetcher;
    }

    @Override
    public List<ServiceMeta.EntryPointMeta> selectAllEndPoint(String serviceType) {
        return entryPointFetcher.getAliveEntryPoints(serviceType);
    }

    public List<ServiceMeta.EntryPointMeta> getAliveEndPoints(
            String serviceType, String component) {
        List<ServiceMeta.EntryPointMeta> entryPointInfoList =
                entryPointFetcher.getAliveEntryPoints(serviceType);
        if (StringUtils.isBlank(component)) {
            return entryPointInfoList;
        }
        List<ServiceMeta.EntryPointMeta> result = new ArrayList<>();
        for (ServiceMeta.EntryPointMeta entryPointMeta : entryPointInfoList) {
            if (entryPointMeta.getComponents().contains(component)) {
                result.add(entryPointMeta);
            }
        }
        return result;
    }

    @Override
    public ServiceMeta.EntryPointMeta selectService(
            Policy policy, String serviceType, String component, String targetId) {
        List<ServiceMeta.EntryPointMeta> entryPointInfoList =
                getAliveEndPoints(serviceType, component);
        // get the nodeInfo with
        if (entryPointInfoList == null || entryPointInfoList.isEmpty()) {
            logger.warn(
                    "selectService: can't find entrypoint for service: {}, component: {}, targetId: {}",
                    serviceType,
                    component,
                    targetId);
            return null;
        }
        if (policy == Policy.ROUND_ROBIN) {
            int idx = lastIdx.addAndGet(1) % entryPointInfoList.size();
            return entryPointInfoList.get(idx);
        }
        // select by hash
        int hashValue = targetId.hashCode();
        int idx = hashValue % entryPointInfoList.size();
        int selectedIdx = Math.max(idx, 0);
        lastIdx.set(selectedIdx);
        ServiceMeta.EntryPointMeta selectedService = entryPointInfoList.get(selectedIdx);
        logger.info(
                "selectService: {}, hashCode: {}, alive-entrypoints: {}, targetId: {}",
                selectedService.toString(),
                hashValue,
                entryPointInfoList.size(),
                targetId);
        return selectedService;
    }
}
