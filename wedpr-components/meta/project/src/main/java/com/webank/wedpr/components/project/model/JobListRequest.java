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
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.components.project.dao.JobDO;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class JobListRequest {
    private List<String> jobs = new ArrayList<>();

    public JobListRequest() {}

    public JobListRequest(Set<JobDO> jobs) {
        for (JobDO job : jobs) {
            this.jobs.add(job.getId());
        }
    }

    public List<String> getJobs() {
        return jobs;
    }

    public void setJobs(List<String> jobs) {
        this.jobs = jobs;
    }

    public static JobListRequest deserialize(String data) throws JsonProcessingException {
        return ObjectMapperFactory.getObjectMapper().readValue(data, JobListRequest.class);
    }

    public String serialize() throws JsonProcessingException {
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
    }
}
