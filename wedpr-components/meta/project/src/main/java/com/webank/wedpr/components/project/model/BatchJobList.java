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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.webank.wedpr.common.protocol.JobStatus;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.components.project.dao.JobDO;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BatchJobList {
    private List<JobDO> jobs;
    private long total;

    public BatchJobList() {}

    public BatchJobList(long total, List<JobDO> jobs) {
        this.jobs = jobs;
        this.total = total;
    }

    public BatchJobList(List<JobDO> jobs) {
        this.jobs = jobs;
    }

    public BatchJobList(Set<JobDO> jobSet) {
        this.jobs = new ArrayList<>(jobSet);
    }

    public void resetStatus(JobStatus jobStatus) {
        if (jobs == null) {
            return;
        }
        for (JobDO jobDO : jobs) {
            jobDO.setStatus(jobStatus.getStatus());
        }
    }

    public List<JobDO> getJobs() {
        return jobs;
    }

    public void setJobs(List<JobDO> jobs) {
        this.jobs = jobs;
    }

    public String serialize() throws JsonProcessingException {
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
    }

    public static BatchJobList deserialize(String data) throws JsonProcessingException {
        return ObjectMapperFactory.getObjectMapper().readValue(data, BatchJobList.class);
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
