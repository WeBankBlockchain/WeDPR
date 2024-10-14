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

import com.webank.wedpr.common.utils.ThreadPoolService;
import com.webank.wedpr.components.leader.election.LeaderElection;
import com.webank.wedpr.components.meta.sys.config.WeDPRSysConfig;
import com.webank.wedpr.components.sync.ResourceSyncer;
import com.webank.wedpr.components.sync.dao.SyncStatusMapperWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ResourceSyncerImpl implements ResourceSyncer {
    private static final Logger logger = LoggerFactory.getLogger(ResourceSyncerImpl.class);
    protected final SyncWorker syncWorker;
    protected final SyncStatusMapperWrapper syncStatusMapper;
    protected final LeaderElection leaderElection;

    public ResourceSyncerImpl(
            WeDPRSysConfig weDPRSysConfig,
            LeaderElection leaderElection,
            SyncStatusMapperWrapper syncStatusMapper,
            ThreadPoolService threadPoolService) {
        ResourceSyncStatus resourceSyncStatus = new ResourceSyncStatus(weDPRSysConfig);
        this.leaderElection = leaderElection;
        this.syncStatusMapper = syncStatusMapper;
        this.syncWorker =
                new SyncWorker(
                        "resource-sync-thread",
                        leaderElection,
                        syncStatusMapper,
                        resourceSyncStatus,
                        threadPoolService);
        // register the handler
        leaderElection.registerOnCampaignHandler(
                new LeaderElection.CampaignHandler() {
                    @Override
                    public void onCampaign(boolean success, String leaderID) {
                        onLeaderSwitch(success, leaderID);
                    }
                });
    }

    @Override
    public LeaderElection getLeaderElection() {
        return leaderElection;
    }

    @Override
    public void onLeaderSwitch(boolean isLeader, String leaderID) {
        this.syncWorker.onLeaderSwitch(isLeader, leaderID);
    }

    // register the resource-commit-handler
    @Override
    public void registerCommitHandler(String resourceType, CommitHandler commitHandler) {
        this.syncWorker.registerCommitHandler(resourceType, commitHandler);
    }

    // register the hook called after commit
    @Override
    public void registerCommitAfterHook(String resourceType, CommitAfterHook hook) {
        this.syncWorker.registerCommitAfterHook(resourceType, hook);
    }

    @Override
    public void start() {

        this.syncWorker.startWorking();
    }

    public void stop() {
        this.syncWorker.stop();
    }
}
