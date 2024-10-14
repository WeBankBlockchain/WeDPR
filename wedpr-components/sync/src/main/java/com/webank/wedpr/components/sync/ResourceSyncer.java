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

package com.webank.wedpr.components.sync;

import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.leader.election.LeaderElection;
import com.webank.wedpr.components.sync.core.ResourceActionRecord;
import com.webank.wedpr.components.sync.core.ResourceActionResult;

public interface ResourceSyncer {
    public static enum ResourceType {
        Authorization("Authorization"),
        Job("Job"),
        Dataset("Dataset"),
        Publish("Publish");
        private final String type;

        ResourceType(String type) {
            this.type = type;
        }

        public String getType() {
            return this.type;
        }
    }

    public static class CommitArgs {
        private final ResourceActionRecord resourceActionRecord;

        public CommitArgs(ResourceActionRecord record) {
            this.resourceActionRecord = record;
        }

        public ResourceActionRecord getResourceActionRecord() {
            return this.resourceActionRecord;
        }
    }

    @FunctionalInterface
    public interface CommitHandler {
        void call(CommitArgs args) throws WeDPRException;
    }

    // sync the resource
    public abstract void sync(String trigger, ResourceActionRecord record);
    // register the commitHandler
    @FunctionalInterface
    public interface CommitAfterHook {
        void call(ResourceActionResult result) throws WeDPRException;
    }
    // register the resource-commit-handler
    public abstract void registerCommitHandler(String resourceType, CommitHandler commitHandler);
    // register the hook called after commit
    public abstract void registerCommitAfterHook(String resourceType, CommitAfterHook hook);

    public abstract void onLeaderSwitch(boolean isLeader, String leaderID);

    public abstract LeaderElection getLeaderElection();

    public abstract void start();
}
