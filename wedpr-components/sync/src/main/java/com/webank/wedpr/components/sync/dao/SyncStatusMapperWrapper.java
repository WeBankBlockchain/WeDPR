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

package com.webank.wedpr.components.sync.dao;

public class SyncStatusMapperWrapper {
    private final SyncStatusMapper syncStatusMapper;

    public SyncStatusMapperWrapper(SyncStatusMapper syncStatusMapper) {
        this.syncStatusMapper = syncStatusMapper;
    }

    public SyncStatusMapper getSyncStatusMapper() {
        return this.syncStatusMapper;
    }

    // Note: when receive ResourceRecordAction from other agency, the record-status are not exist in
    // local wedpr_sync_status_table table
    public int setSyncRecordStatus(ResourceActionDO resourceActionDO) {
        ResourceActionDO condition = new ResourceActionDO(resourceActionDO.getResourceID());
        Integer result = this.syncStatusMapper.queryResourceRecordCount(condition);
        // insert the record
        if (result == 0) {
            return this.syncStatusMapper.insertResourceRecordMeta(resourceActionDO);
        }
        // update the record
        return this.syncStatusMapper.updateResourceRecordMeta(resourceActionDO);
    }
}
