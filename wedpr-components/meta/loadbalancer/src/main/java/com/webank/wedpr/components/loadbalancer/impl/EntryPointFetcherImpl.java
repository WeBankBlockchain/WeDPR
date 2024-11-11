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
import com.webank.wedpr.sdk.jni.transport.WeDPRTransport;
import com.webank.wedpr.sdk.jni.transport.model.ServiceMeta;
import java.util.List;

public class EntryPointFetcherImpl implements EntryPointFetcher {
    private final WeDPRTransport weDPRTransport;

    public EntryPointFetcherImpl(WeDPRTransport weDPRTransport) {
        this.weDPRTransport = weDPRTransport;
    }

    @Override
    public List<ServiceMeta.EntryPointMeta> getAliveEntryPoints(String serviceName) {
        return this.weDPRTransport.getAliveEntryPoints(serviceName);
    }
}