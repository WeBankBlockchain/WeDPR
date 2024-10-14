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
package com.webank.wedpr.components.sync.impl;

import com.webank.wedpr.common.protocol.SysConfigKey;
import com.webank.wedpr.components.meta.sys.config.WeDPRSysConfig;
import com.webank.wedpr.components.sync.core.ResourceActionRecord;
import java.math.BigInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceSyncStatus {
    private static final Logger logger = LoggerFactory.getLogger(ResourceSyncStatus.class);
    private BigInteger resourceLogIndex;
    private BigInteger expectedResourceLogIndex;
    private BigInteger blockNumber;
    private final transient WeDPRSysConfig weDPRSysConfig;

    public ResourceSyncStatus(WeDPRSysConfig weDPRSysConfig) {
        this.weDPRSysConfig = weDPRSysConfig;
        fetchResourceLogIndex();
    }

    public void fetchResourceLogIndex() {
        this.resourceLogIndex =
                new BigInteger(
                        this.weDPRSysConfig
                                .getConfig(SysConfigKey.getResourceLogIndexKey())
                                .getConfigValue());
        this.blockNumber =
                new BigInteger(
                        this.weDPRSysConfig
                                .getConfig(SysConfigKey.getSyncedBlockNumber())
                                .getConfigValue());
        this.expectedResourceLogIndex = resourceLogIndex.add(BigInteger.ONE);
        logger.info(
                "ResourceSyncStatus fetchResourceLogIndex, resourceLogIndex: {}, blockNumber: {}",
                resourceLogIndex,
                blockNumber);
    }

    public BigInteger getResourceLogIndex() {
        return resourceLogIndex;
    }

    public BigInteger getBlockNumber() {
        return blockNumber;
    }

    public BigInteger getExpectedResourceLogIndex() {
        return expectedResourceLogIndex;
    }

    public WeDPRSysConfig getWeDPRSysConfig() {
        return this.weDPRSysConfig;
    }

    public synchronized void finalizeResource(ResourceActionRecord resourceActionRecord) {
        if (resourceLogIndex.compareTo(resourceActionRecord.getIndex()) < 0) {
            resourceLogIndex = resourceActionRecord.getIndex();
            this.expectedResourceLogIndex = resourceLogIndex.add(BigInteger.ONE);
            this.weDPRSysConfig.updateConfig(
                    SysConfigKey.getResourceLogIndexKey(), String.valueOf(resourceLogIndex));
        }
        if (blockNumber.compareTo(resourceActionRecord.getBlockNumber()) < 0) {
            blockNumber = resourceActionRecord.getBlockNumber();
            this.weDPRSysConfig.updateConfig(
                    SysConfigKey.getSyncedBlockNumber(), String.valueOf(blockNumber));
        }
        logger.info(
                "finalizeResource ResourceSyncStatus, resourceID: {}, logIndex: {}, blockIndex: {}, updated resourceLogIndex: {}, updated blockNumber: {}",
                resourceActionRecord.getResourceID(),
                resourceActionRecord.getIndex(),
                resourceActionRecord.getBlockNumber(),
                resourceLogIndex,
                blockNumber);
    }

    @Override
    public String toString() {
        return "ResourceSyncStatus{"
                + "blockNumber="
                + blockNumber
                + ", weDPRSysConfig="
                + weDPRSysConfig
                + '}';
    }
}
