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

import com.webank.wedpr.common.protocol.JobStatus;
import com.webank.wedpr.common.protocol.ReportStatusEnum;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.meta.resource.follower.dao.FollowerMapper;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProjectMapperWrapper {
    private static final Logger logger = LoggerFactory.getLogger(ProjectMapperWrapper.class);

    @Autowired private ProjectMapper projectMapper;
    @Autowired private FollowerMapper followerMapper;

    public List<JobDO> queryJobByCondition(
            Boolean onlyMeta, String user, String agency, JobDO condition) {
        // query with the owner identity
        condition.setOwner(user);
        condition.setOwnerAgency(agency);
        return this.projectMapper.queryJobs(onlyMeta, condition, null);
    }

    public Long queryTotalJobCount(String user, String agency, JobDO condition) {
        // query the submitted job
        condition.setOwner(user);
        condition.setOwnerAgency(agency);
        Long submittedJobCount = this.projectMapper.queryJobCount(condition);
        // reset the owner information
        condition.setOwner("");
        condition.setOwnerAgency("");
        Long followerJobCount = this.projectMapper.queryFollowerJobCount(user, agency, condition);
        return submittedJobCount + followerJobCount;
    }

    public Set<JobDO> queryJobsByCondition(
            Boolean onlyMeta, String user, String agency, JobDO condition) {
        // query the job for given user and agency
        List<JobDO> jobList = new ArrayList<>();
        List<JobDO> jobs = queryJobByCondition(onlyMeta, user, agency, condition);
        if (jobs != null && !jobs.isEmpty()) {
            jobList.addAll(jobs);
        }
        if (condition.getLimitItems() > 0) {
            if (jobList.size() >= condition.getLimitItems()) {
                return new HashSet<>(jobList);
            }
            condition.setLimitItems(condition.getLimitItems() - jobList.size());
        }
        return new HashSet<>(jobList);
    }

    public Set<JobDO> queryJobMetasByStatus(String user, String agency, String status) {
        JobDO condition = new JobDO(true);
        condition.setStatus(status);
        return queryJobsByCondition(true, user, agency, condition);
    }

    public void updateSingleJobStatus(String user, String agency, JobDO jobDO, JobStatus status) {
        batchUpdateJobStatus(user, agency, new ArrayList<>(Arrays.asList(jobDO)), status);
    }

    public List<JobDO> queryJobDetail(String jobID, String user, String agency) {
        JobDO condition = new JobDO(jobID);
        List<JobDO> jobDOList = this.queryJobByCondition(false, user, agency, condition);
        // try to query the follower information
        if (jobDOList == null || jobDOList.isEmpty()) {
            // reset the condition
            condition.setOwner(null);
            condition.setOwnerAgency(null);
            jobDOList =
                    this.projectMapper.queryFollowerJobByCondition(false, user, agency, condition);
        }
        return jobDOList;
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateJobStatus(
            String user, String agency, List<JobDO> jobs, JobStatus status) {
        if (jobs == null || jobs.isEmpty()) {
            return;
        }
        // batch update the job status
        List<JobDO> updatedJobList = new ArrayList<>();
        for (JobDO job : jobs) {
            JobDO updatedJob = new JobDO(job.getId());
            updatedJob.setStatus(status.getStatus());
            updatedJob.setOwner(user);
            updatedJob.setOwnerAgency(agency);
            updatedJob.setReportStatus(ReportStatusEnum.NO_REPORT.getReportStatus());
            updatedJobList.add(updatedJob);
            this.projectMapper.updateJobInfo(updatedJob);
        }

        logger.info(
                "batch update job status, user: {}, agency: {}, jobsSize: {}",
                user,
                agency,
                updatedJobList.size());
        // this.projectMapper.batchUpdateJobInfo(updatedJobList);
    }

    public WeDPRResponse updateJobStatus(
            String user, String agency, List<String> jobs, JobStatus status) {
        WeDPRResponse response =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        try {
            if (jobs == null || jobs.isEmpty()) {
                return response;
            }
            List<JobDO> jobDOList = new ArrayList<>();
            for (String id : jobs) {
                jobDOList.add(new JobDO(id));
            }
            batchUpdateJobStatus(user, agency, jobDOList, status);
            return response;
        } catch (Exception e) {
            logger.warn("updateJobStatus exception, user: {}, error: ", user, e);
            response.setData(Constant.WEDPR_FAILED);
            response.setMsg("updateJobStatus failed for " + e.getMessage());
        }
        return response;
    }

    public void updateJob(JobDO jobDO) {
        jobDO.setReportStatus(ReportStatusEnum.NO_REPORT.getReportStatus());
        this.projectMapper.batchUpdateJobInfo(new ArrayList<>(Collections.singletonList(jobDO)));
    }

    public void recordJobStatus(JobDO jobDO) {
        JobDO condition = new JobDO(true);
        condition.setId(jobDO.getId());
        List<JobDO> queriedResult = this.projectMapper.queryJobs(false, condition, null);
        if (queriedResult == null || queriedResult.isEmpty()) {
            this.insertJob(jobDO);
            return;
        }
        jobDO.setReportStatus(ReportStatusEnum.NO_REPORT.getReportStatus());
        this.projectMapper.batchUpdateJobInfo(new ArrayList<>(Collections.singletonList(jobDO)));
    }

    @Transactional(rollbackFor = Exception.class)
    public void insertJob(JobDO jobDO) {
        String id = jobDO.getId();

        // logger.info(" ## insert job, jobID: {}", id);

        this.projectMapper.insertJobInfo(jobDO);

        int insertC = -1;
        List<String> datasetList = jobDO.getDatasetList();
        if (datasetList != null && !datasetList.isEmpty()) {
            insertC = this.projectMapper.batchInsertJobDatasetRelationInfo(id, datasetList);
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "batch insert job datasets relation, jobID: {}, datasetIDs: {}, insertCount: {}",
                    id,
                    datasetList,
                    insertC);
        }

        if (jobDO.getTaskParties() == null || jobDO.getTaskParties().isEmpty()) {
            return;
        }
        this.followerMapper.batchInsert(jobDO.getTaskParties());
    }

    public void updateFinalJobResult(JobDO job, JobStatus status, String result) {
        job.getJobResult().setJobStatus(status);
        job.getJobResult().setResult(result);
        job.setResult(job.getJobResult().serialize());
        updateJobResult(job.getId(), status, job.getResult());
    }

    public void updateJobResult(String jobID, JobStatus status, String result) {
        JobDO updatedJob = new JobDO(jobID);
        if (status != null) {
            updatedJob.setStatus(status.getStatus());
        }
        updatedJob.setResult(result);
        updateJob(updatedJob);
    }

    public ProjectMapper getProjectMapper() {
        return projectMapper;
    }

    public FollowerMapper getFollowerMapper() {
        return followerMapper;
    }
}
