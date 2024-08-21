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

import com.webank.wedpr.components.sync.core.ResourceActionRecord;

public class ResourceActionDO extends ResourceActionRecord {
    private String status;
    private String createTime;
    private String lastUpdateTime;
    private String statusMsg;
    private String startTime;
    private String endTime;

    public ResourceActionDO() {}

    public ResourceActionDO(String resourceID) {
        setResourceID(resourceID);
    }

    public ResourceActionDO(String resourceID, String status) {
        setResourceID(resourceID);
        setStatus(status);
    }

    public ResourceActionDO(ResourceActionRecord record) {
        setResourceID(record.getResourceID());
        setAgency(record.getAgency());
        setTrigger(record.getTrigger());
        setResourceType(record.getResourceType());
        setResourceAction(record.getResourceAction());
        setIndex(record.getIndex());
        setBlockNumber(record.getBlockNumber());
        setTransactionHash(record.getTransactionHash());
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(String statusMsg) {
        this.statusMsg = statusMsg;
    }

    @Override
    public String getStartTime() {
        return startTime;
    }

    @Override
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    @Override
    public String getEndTime() {
        return endTime;
    }

    @Override
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
