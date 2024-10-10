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

package com.webank.wedpr.components.project.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.wedpr.components.meta.resource.follower.dao.FollowerDO;
import com.webank.wedpr.components.uuid.generator.WeDPRUuidGenerator;
import com.webank.wedpr.core.protocol.JobStatus;
import com.webank.wedpr.core.protocol.JobType;
import com.webank.wedpr.core.utils.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

@EqualsAndHashCode(callSuper = true)
@Data
public class JobDO extends TimeRange {
    @Data
    @ToString
    public static class JobResultItem {
        private String jobID;
        private Boolean success;
        private String result;
        private Long startTime = 0L;
        private Long timeCostMs = 0L;

        public JobResultItem() {}

        public JobResultItem(String jobID, Boolean success, String result) {
            this.jobID = jobID;
            this.success = success;
            this.result = result;
        }

        public void setStartTime(Long startTime) {
            if (startTime == null) {
                return;
            }
            this.startTime = startTime;
        }

        public void setTimeCostMs(Long timeCostMs) {
            if (timeCostMs == null) {
                return;
            }
            this.timeCostMs = timeCostMs;
        }
    }

    @Data
    @ToString
    public static class JobResult {
        @JsonProperty("status")
        private JobStatus jobStatus;

        @JsonProperty("statusDetail")
        private String result;

        @JsonProperty("subJobStatusInfos")
        private Map<String, JobDO.JobResultItem> subJobResults = new HashMap<>();

        private Long timeCostMs = 0L;

        public void setSubJobResults(Map<String, JobDO.JobResultItem> subJobResults) {
            if (subJobResults == null) {
                return;
            }
            this.subJobResults = subJobResults;
            for (String subJob : subJobResults.keySet()) {
                this.timeCostMs += subJobResults.get(subJob).getTimeCostMs();
            }
        }

        public void setTimeCostMs(Long timeCostMs) {
            if (timeCostMs == null) {
                return;
            }
            this.timeCostMs = timeCostMs;
        }

        @SneakyThrows(Exception.class)
        public String serialize() {
            return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
        }

        public static JobResult deserialize(String data) {
            try {
                if (StringUtils.isBlank(data)) {
                    return null;
                }
                return ObjectMapperFactory.getObjectMapper().readValue(data, JobResult.class);
            } catch (Exception e) {
                return null;
            }
        }
    }

    private String id = WeDPRUuidGenerator.generateID();
    private String name = "";
    private String parties;
    // the job type
    private String jobType;
    private Integer reportStatus;

    @JsonIgnore private transient JobType type;
    @JsonIgnore private transient JobType originalJobType;

    @JsonIgnore private transient Object jobParam;
    private String owner;
    private String ownerAgency;
    private String projectName;
    private String param;

    @JsonIgnore private transient Object jobRequest;
    private String status;
    @JsonIgnore private String result;
    private List<String> datasetList;

    @JsonProperty("jobStatusInfo")
    private JobResult jobResult = new JobResult();

    @JsonIgnore private transient List<FollowerDO> taskParties;

    @JsonIgnore private transient Integer limitItems = -1;

    // shouldSync or not
    private Boolean shouldSync;

    private String createTime;
    private String lastUpdateTime;

    public JobDO() {}

    public JobDO(boolean resetID) {
        if (resetID) {
            this.id = "";
        }
    }

    public JobDO(String owner, String ownerAgency) {
        setOwner(owner);
        setOwnerAgency(ownerAgency);
    }

    public JobDO(String id) {
        setId(id);
    }

    public void setName(String name) {
        if (name == null) {
            return;
        }
        this.name = name;
    }

    public void setResult(String result) {
        this.result = result;
        this.jobResult = JobResult.deserialize(result);
    }

    public void setJobResult(JobResult jobResult) {
        if (jobResult == null) {
            return;
        }
        this.jobResult = jobResult;
        this.result = this.jobResult.serialize();
    }

    public void updateSubJobResult(JobResultItem jobResultItem) {
        this.jobResult.getSubJobResults().put(jobResultItem.getJobID(), jobResultItem);
        this.result = this.jobResult.serialize();
    }

    public boolean skipTask(String taskID) {
        if (this.jobResult == null) {
            return false;
        }
        if (this.jobResult.getSubJobResults() == null) {
            return false;
        }
        if (!this.jobResult.getSubJobResults().containsKey(taskID)) {
            return false;
        }
        if (!this.jobResult.getSubJobResults().get(taskID).getSuccess()) {
            return false;
        }
        return true;
    }

    public void setShouldSync(Boolean shouldSync) {
        if (shouldSync == null) {
            return;
        }
        this.shouldSync = shouldSync;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
        if (StringUtils.isBlank(this.jobType)) {
            return;
        }
        this.type = JobType.deserialize(this.jobType);
        if (this.type != null) {
            this.shouldSync = this.type.shouldSync();
        }
    }

    public void setType(JobType type) {
        this.type = type;
        if (type == null) {
            return;
        }
        this.shouldSync = type.shouldSync();
        this.jobType = type.getType();
    }

    public List<FollowerDO> getTaskParties() {
        return taskParties;
    }

    @SneakyThrows(Exception.class)
    public void setTaskParties(List<FollowerDO> taskParties) {
        this.taskParties = taskParties;
        if (taskParties == null) {
            return;
        }
        for (FollowerDO followerDO : taskParties) {
            followerDO.setResourceID(id);
        }
        this.parties = ObjectMapperFactory.getObjectMapper().writeValueAsString(taskParties);
    }

    public String getParties() {
        return parties;
    }

    @SneakyThrows(Exception.class)
    public void setParties(String parties) {
        this.parties = parties;
        if (StringUtils.isBlank(this.parties)) {
            return;
        }
        this.taskParties =
                ObjectMapperFactory.getObjectMapper()
                        .readValue(parties, new TypeReference<List<FollowerDO>>() {});
    }

    public void checkCreate() throws Exception {
        // check parties
        if (this.taskParties == null) {
            throw new WeDPRException("Invalid job for the relevant parties are not defined!");
        }
        // check jobType
        if (this.getType() == null || StringUtils.isBlank(this.getJobType())) {
            throw new WeDPRException(
                    "Invalid job for the job type " + this.getJobType() + " is not supported!");
        }
        // check the status
        if (!StringUtils.isBlank(this.getStatus())) {
            throw new WeDPRException("Invalid submitJob request, not permit to set the status");
        }
        // check the result
        if (!StringUtils.isBlank(this.getResult())) {
            throw new WeDPRException("Invalid submitJob request, not permit to set the result");
        }
        Common.requireNonEmpty("projectName", projectName);
        Common.requireNonEmpty("param", param);
    }

    public static JobDO deserialize(String data) throws JsonProcessingException {
        return ObjectMapperFactory.getObjectMapper().readValue(data, JobDO.class);
    }

    public Boolean isJobParty(String agency) {
        if (this.ownerAgency.compareToIgnoreCase(agency) == 0) {
            return Boolean.TRUE;
        }
        for (FollowerDO followerDO : taskParties) {
            if (followerDO.getAgency().compareToIgnoreCase(agency) == 0) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    // TODO: verify dataset
    public boolean isJobDataset(String datasetId) {
        if (datasetList == null) {
            return false;
        }

        return datasetList.contains(datasetId);
    }

    // Note: here taskID add taskType postfix in case of the job rejected by the model_node
    // for taskID conflict when executing different types of sub-tasks
    public String getTaskID() {
        if (StringUtils.isBlank(this.id)) {
            return this.id;
        }
        return this.id + Constant.DEFAULT_SPLITTER + jobType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobDO jobDO = (JobDO) o;
        return Objects.equals(id, jobDO.id)
                && Objects.equals(name, jobDO.name)
                && Objects.equals(parties, jobDO.parties)
                && Objects.equals(jobType, jobDO.jobType)
                && Objects.equals(owner, jobDO.owner)
                && Objects.equals(ownerAgency, jobDO.ownerAgency)
                && Objects.equals(projectName, jobDO.projectName)
                && Objects.equals(status, jobDO.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, parties, jobType, owner, ownerAgency, projectName, status);
    }

    @Override
    public String toString() {
        return "JobDO{"
                + "id='"
                + id
                + '\''
                + ", name='"
                + name
                + '\''
                + ", owner='"
                + owner
                + '\''
                + ", ownerAgency='"
                + ownerAgency
                + '\''
                + ", projectName='"
                + projectName
                + '\''
                + ", param='"
                + param
                + '\''
                + ", status='"
                + status
                + '\''
                + ", result='"
                + result
                + '\''
                + ", taskParties="
                + taskParties
                + '}';
    }
}
