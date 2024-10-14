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

package com.webank.wedpr.components.project.model;

import com.webank.wedpr.common.utils.PageRequest;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.meta.resource.follower.dao.FollowerDO;
import com.webank.wedpr.components.project.dao.JobDO;
import java.util.List;
import lombok.SneakyThrows;

public class JobRequest extends PageRequest {
    private JobDO job = new JobDO(true);
    private List<FollowerDO> taskParties;
    private List<String> datasetList;

    public JobRequest() {}

    public JobDO getJob() {
        return job;
    }

    public void setJob(JobDO job) {
        if (job == null) {
            return;
        }
        this.job = job;
    }

    public List<FollowerDO> getTaskParties() {
        return taskParties;
    }

    public void setTaskParties(List<FollowerDO> taskParties) {
        this.taskParties = taskParties;
        checkAndConfigTaskParities(taskParties);
    }

    @SneakyThrows(WeDPRException.class)
    private void checkAndConfigTaskParities(List<FollowerDO> taskParties) {
        if (taskParties == null || taskParties.isEmpty()) {
            return;
        }
        for (FollowerDO taskPartyInfo : taskParties) {
            if (taskPartyInfo.getUserName().isEmpty() || taskPartyInfo.getAgency().isEmpty()) {
                throw new WeDPRException(
                        "Invalid party information, should set both the party user and the agency! current partyInfo: "
                                + taskPartyInfo.toString());
            }
            taskPartyInfo.setFollowerType(FollowerDO.FollowerType.JOB.getType());
        }
    }

    public List<String> getDatasetList() {
        return datasetList;
    }

    public void setDatasetList(List<String> datasetList) {
        this.datasetList = datasetList;
    }

    @Override
    public String toString() {
        return "JobRequest{"
                + "job="
                + job
                + ", taskParties="
                + taskParties
                + ", datasetList="
                + datasetList
                + '}';
    }
}
