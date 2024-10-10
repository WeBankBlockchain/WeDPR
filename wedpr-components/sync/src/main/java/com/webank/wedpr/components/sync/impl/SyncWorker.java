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

import com.webank.wedpr.components.leader.election.LeaderElection;
import com.webank.wedpr.components.sync.ResourceSyncer;
import com.webank.wedpr.components.sync.core.ResourceActionRecord;
import com.webank.wedpr.components.sync.dao.SyncStatusMapperWrapper;
import com.webank.wedpr.core.utils.ThreadPoolService;
import com.webank.wedpr.core.utils.Worker;
import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyncWorker extends Worker {
    private static final Logger logger = LoggerFactory.getLogger(SyncWorker.class);
    // thread-safe
    private final PriorityBlockingQueue<ResourceActionRecord> resourceQueue =
            new PriorityBlockingQueue<>(
                    WeDPRSyncConfig.getResourceSyncQueueLimit(),
                    new Comparator<ResourceActionRecord>() {
                        @Override
                        public int compare(ResourceActionRecord o1, ResourceActionRecord o2) {
                            return o1.getIndex().compareTo(o2.getIndex());
                        }
                    });

    private final ResourceSyncStatus resourceSyncStatus;
    private final ResourceExecutor resourceExecutor;
    private final LeaderElection leaderElection;

    public SyncWorker(
            String workerName,
            LeaderElection leaderElection,
            SyncStatusMapperWrapper syncStatusMapper,
            ResourceSyncStatus resourceSyncStatus,
            ThreadPoolService threadPoolService) {
        super(workerName, WeDPRSyncConfig.getWorkerIdleWaitMs());
        this.leaderElection = leaderElection;
        this.resourceSyncStatus = resourceSyncStatus;
        this.resourceExecutor =
                new ResourceExecutor(resourceSyncStatus, syncStatusMapper, threadPoolService);
    }

    // addElement to the queue
    public void push(ResourceActionRecord resourceActionRecord) {
        if (!this.leaderElection.isLeader()) {
            logger.info(
                    "The follower drop the received resourceActionRecord, resourceID: {}",
                    resourceActionRecord.getResourceID());
            return;
        }
        resourceQueue.put(resourceActionRecord);
        logger.info(
                "Push ResourceActionRecord: {} to queue success, expectedIndex: {}",
                resourceActionRecord.toString(),
                resourceSyncStatus.getExpectedResourceLogIndex());
        wakeupWorker();
    }

    public int getUnHandledResourceRecordSize() {
        return this.resourceQueue.size();
    }
    // register the resource-commit-handler
    public void registerCommitHandler(
            String resourceType, ResourceSyncer.CommitHandler commitHandler) {
        this.resourceExecutor.registerCommitHandler(resourceType, commitHandler);
    }

    // register the hook called after commit
    public void registerCommitAfterHook(String resourceType, ResourceSyncer.CommitAfterHook hook) {
        this.resourceExecutor.registerCommitAfterHook(resourceType, hook);
    }

    // called when the leader switch
    public void onLeaderSwitch(boolean isLeader, String leaderID) {
        if (isLeader) {
            // recover the latest status
            this.resourceSyncStatus.fetchResourceLogIndex();
            return;
        }
        // switch to follower
        logger.info(
                "onLeaderSwitch: switch to follower, clear the un-committed resource-action-recorders, cleared-size: {}",
                resourceQueue.size());
        resourceQueue.clear();
    }

    @Override
    protected void execute() {
        if (!this.leaderElection.isLeader()) {
            return;
        }
        if (resourceQueue.isEmpty()) {
            return;
        }
        while (!resourceQueue.isEmpty()) {
            ResourceActionRecord record = resourceQueue.peek();
            if (record.getIndex().compareTo(resourceSyncStatus.getExpectedResourceLogIndex()) < 0) {
                logger.info(
                        "Receive committed resource, resourceIndex: {}, currentSyncStatus: {}",
                        record.getIndex(),
                        resourceSyncStatus.toString());
                resourceQueue.poll();
                continue;
            }
            if (record.getIndex().compareTo(resourceSyncStatus.getExpectedResourceLogIndex())
                    != 0) {
                break;
            }
            // commit the record
            resourceExecutor.commitResourceMeta(record);
            resourceQueue.poll();
        }
    }

    public ResourceSyncStatus getResourceSyncStatus() {
        return resourceSyncStatus;
    }
}
