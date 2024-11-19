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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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

    @Override
    public ServiceMeta.EntryPointMeta selectService(
            Policy policy, String serviceType, String targetId) {
        List<ServiceMeta.EntryPointMeta> entryPointInfoList =
                entryPointFetcher.getAliveEntryPoints(serviceType);
        if (entryPointInfoList == null || entryPointInfoList.isEmpty()) {
            logger.warn(
                    "selectService: can't find entrypoint for service: {}, targetId: {}",
                    serviceType,
                    targetId);
            return null;
        }
        if (policy == Policy.ROUND_ROBIN) {
            int idx = lastIdx.addAndGet(1) % entryPointInfoList.size();
            return entryPointInfoList.get(idx);
        }
        // select by hash
        int idx = targetId.hashCode() % entryPointInfoList.size();
        int selectedIdx = Math.max(idx, 0);
        lastIdx.set(selectedIdx);
        logger.info("selectService: {}", entryPointInfoList.get(selectedIdx).toString());
        return entryPointInfoList.get(selectedIdx);
    }
}
