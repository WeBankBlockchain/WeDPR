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

import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.ThreadPoolService;
import com.webank.wedpr.components.sync.ResourceSyncer;
import com.webank.wedpr.components.sync.core.ResourceActionRecord;
import com.webank.wedpr.components.sync.core.ResourceActionResult;
import com.webank.wedpr.components.sync.dao.ResourceActionDOBuilder;
import com.webank.wedpr.components.sync.dao.SyncStatusMapperWrapper;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceExecutor {
    private static final Logger logger = LoggerFactory.getLogger(ResourceExecutor.class);
    protected ConcurrentHashMap<String, SyncHandler> resourceHandlers = new ConcurrentHashMap<>();

    private final ResourceSyncStatus resourceSyncStatus;
    private final SyncStatusMapperWrapper syncStatusMapper;
    private final ThreadPoolService threadPoolService;

    public ResourceExecutor(
            ResourceSyncStatus resourceSyncStatus,
            SyncStatusMapperWrapper syncStatusMapper,
            ThreadPoolService threadPoolService) {
        this.resourceSyncStatus = resourceSyncStatus;
        this.syncStatusMapper = syncStatusMapper;
        this.threadPoolService = threadPoolService;
    }

    // register the resource-commit-handler
    public void registerCommitHandler(
            String resourceType, ResourceSyncer.CommitHandler commitHandler) {
        logger.info("registerCommitHandler for {}", resourceType);
        if (!resourceHandlers.containsKey(resourceType)) {
            resourceHandlers.put(resourceType, new SyncHandler());
        }
        resourceHandlers.get(resourceType).setCommitHandler(commitHandler);
    }

    // register the hook called after commit
    public void registerCommitAfterHook(String resourceType, ResourceSyncer.CommitAfterHook hook) {
        logger.info("registerCommitAfterHook for {}", resourceType);
        if (!resourceHandlers.containsKey(resourceType)) {
            resourceHandlers.put(resourceType, new SyncHandler());
        }
        resourceHandlers.get(resourceType).setCommitAfterHook(hook);
    }

    // TODO: support concurrent commit
    public void commitResourceMeta(ResourceActionRecord resourceActionRecord) {
        ResourceActionResult result = new ResourceActionResult();
        try {
            logger.info("commitResourceMeta: {}", resourceActionRecord.toString());
            if (!resourceHandlers.containsKey(resourceActionRecord.getResourceType())
                    || resourceHandlers
                                    .get(resourceActionRecord.getResourceType())
                                    .getCommitHandler()
                            == null) {
                logger.warn(
                        "The commitHandler for resource type {} has not been defined, return without commit directly, resourceIndex: {}",
                        resourceActionRecord.getResourceType(),
                        resourceActionRecord.getIndex());
                this.resourceSyncStatus.finalizeResource(resourceActionRecord);
                return;
            }
            long startT = System.currentTimeMillis();
            // call the commitHandler
            logger.info("begin to commit resource: {}", resourceActionRecord.getResourceID());
            ResourceSyncer.CommitHandler commitHandler =
                    resourceHandlers.get(resourceActionRecord.getResourceType()).getCommitHandler();
            commitHandler.call(new ResourceSyncer.CommitArgs(resourceActionRecord));
            logger.info(
                    "commit resource success, id: {}, timecost(ms): {}",
                    resourceActionRecord.getResourceID(),
                    System.currentTimeMillis() - startT);
            this.resourceSyncStatus.finalizeResource(resourceActionRecord);
            // Note: since the node maybe the on-chain node, should update the blockInformation
            // after on-chain success
            this.syncStatusMapper.setSyncRecordStatus(
                    ResourceActionDOBuilder.build(
                            ResourceRecordStatus.COMMIT_SUCCESS.getStatus(),
                            resourceActionRecord,
                            Constant.WEDPR_SUCCESS_MSG));
            logger.info("commitResourceMeta success: {}", resourceActionRecord.toString());
        } catch (Exception e) {
            result.setCode(-1);
            result.setMsg(e.getMessage());
            result.setE(e);
            logger.info(
                    "commitResourceMeta failed, resource: {}, error: ",
                    resourceActionRecord.toString(),
                    e);
            String errorMsg =
                    "Commit failed for resource: "
                            + resourceActionRecord.getResourceID()
                            + ", error: "
                            + e.getLocalizedMessage();
            this.resourceSyncStatus.finalizeResource(resourceActionRecord);
            // Note: since the node maybe the on-chain node, should update the blockInformation
            // after on-chain success
            this.syncStatusMapper.setSyncRecordStatus(
                    ResourceActionDOBuilder.build(
                            ResourceRecordStatus.COMMIT_FAILED.getStatus(),
                            resourceActionRecord,
                            errorMsg));
        }
        executeCommitAfterHook(result, resourceActionRecord);
    }

    private void executeCommitAfterHook(
            ResourceActionResult result, ResourceActionRecord resourceActionRecord) {
        // call the commitAfterHook
        ResourceSyncer.CommitAfterHook hook =
                resourceHandlers.get(resourceActionRecord.getResourceType()).getCommitAfterHook();
        if (hook == null) {
            return;
        }
        threadPoolService
                .getThreadPool()
                .execute(
                        new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    hook.call(result);
                                } catch (Exception e) {
                                    logger.error(
                                            "call CommitAfterHook for {} failed, error: ",
                                            resourceActionRecord.getResourceID(),
                                            e);
                                }
                            }
                        });
    }
}
