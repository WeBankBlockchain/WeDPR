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

package com.webank.wedpr.components.project.service.impl;

import com.github.pagehelper.PageInfo;
import com.webank.wedpr.components.mybatis.PageHelperWrapper;
import com.webank.wedpr.components.project.JobChecker;
import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.project.dao.ProjectDO;
import com.webank.wedpr.components.project.dao.ProjectMapperWrapper;
import com.webank.wedpr.components.project.model.*;
import com.webank.wedpr.components.project.service.ProjectService;
import com.webank.wedpr.core.config.WeDPRCommonConfig;
import com.webank.wedpr.core.protocol.JobStatus;
import com.webank.wedpr.core.utils.Constant;
import com.webank.wedpr.core.utils.TimeRange;
import com.webank.wedpr.core.utils.WeDPRException;
import com.webank.wedpr.core.utils.WeDPRResponse;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ibatis.reflection.ArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImpl implements ProjectService {
    private static final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);
    @Autowired private ProjectMapperWrapper projectMapperWrapper;
    @Autowired private JobChecker jobChecker;

    // create a new project
    @Override
    public WeDPRResponse createProject(String user, ProjectRequest projectDetail) {
        WeDPRResponse response =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        try {
            // set the user
            projectDetail.getProject().setOwner(user);
            projectDetail.getProject().setOwnerAgency(WeDPRCommonConfig.getAgency());
            // check
            projectDetail.getProject().checkCreate();
            // check the existence
            ProjectDO condition = new ProjectDO(projectDetail.getProject().getName());
            condition.setId(null);
            List<ProjectDO> existedProjects =
                    this.projectMapperWrapper
                            .getProjectMapper()
                            .queryProject(Boolean.TRUE, condition);
            if (existedProjects != null && !existedProjects.isEmpty()) {
                throw new WeDPRException(
                        "createProject failed for the project "
                                + projectDetail.getProject().getName()
                                + " already exists, please try another project name!");
            }
            this.projectMapperWrapper
                    .getProjectMapper()
                    .insertProjectInfo(projectDetail.getProject());
            // TODO: init the project resource
            logger.info("createProject success, detail: {}", projectDetail.toString());
            response.setData(projectDetail.getProject().getId());
        } catch (Exception e) {
            logger.warn(
                    "createProject failed, user: {}, detail: {}, error: ",
                    user,
                    projectDetail.toString(),
                    e);
            response.setCode(Constant.WEDPR_FAILED);
            response.setMsg(
                    "Create project failed for "
                            + e.getMessage()
                            + ", project information: "
                            + projectDetail.toString());
        }
        return response;
    }

    // update the project information
    @Override
    public WeDPRResponse updateProject(String user, ProjectRequest updatedProject) {
        WeDPRResponse response =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        try {
            int result =
                    this.projectMapperWrapper
                            .getProjectMapper()
                            .updateProjectInfo(user, updatedProject.getProject());
            logger.info(
                    "updateProject, updated-record: {}, detail: {}",
                    result,
                    updatedProject.toString());
            if (result > 0) {
                return response;
            }
            throw new WeDPRException(
                    "updateProject failed for no project with id "
                            + updatedProject.getProject().getId()
                            + " found for user "
                            + user);
        } catch (Exception e) {
            logger.warn(
                    "updateProject exception, user: {}, updateRecord: {}, error: ",
                    user,
                    updatedProject.toString(),
                    e);
            response.setCode(Constant.WEDPR_FAILED);
            response.setMsg(
                    "updateProject failed for user " + user + ", reason: " + e.getMessage());
        }
        return response;
    }

    // delete the project
    // Note: only the owner can delete the projects
    @Override
    public WeDPRResponse deleteProject(String user, List<String> projectIDList) {
        WeDPRResponse response =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        try {
            int result =
                    this.projectMapperWrapper
                            .getProjectMapper()
                            .deleteProjects(user, projectIDList);
            // TODO: delete the project resource
            logger.info(
                    "deleteProject success, user: {}, projectIDList: {}, deletedNum: {}",
                    user,
                    ArrayUtil.toString(projectIDList),
                    result);
            return response;
        } catch (Exception e) {
            logger.warn(
                    "deleteProject failed, user: {}, projectIDList: {}, error: ",
                    user,
                    ArrayUtil.toString(projectIDList),
                    e);
            response.setCode(Constant.WEDPR_FAILED);
            response.setMsg(
                    "deleteProject for user "
                            + user
                            + " failed, reason: "
                            + e.getMessage()
                            + ", projectIDList: "
                            + ArrayUtil.toString(projectIDList));
        }
        return response;
    }

    @Override
    public Object queryProjectOverview(String user, ProjectOverviewRequest request)
            throws Exception {
        request.check();
        ProjectOverviewResponse response = new ProjectOverviewResponse();
        ProjectDO condition = new ProjectDO();
        condition.setOwner(user);
        condition.setOwnerAgency(WeDPRCommonConfig.getAgency());
        // query the project count
        response.setTotalCount(
                this.projectMapperWrapper.getProjectMapper().queryProjectCount(condition));
        if (request.getStatTime() == null) {
            return response;
        }
        // query the projectStat
        long stepNum = 0;
        Pair<String, String> timeRange = null;
        while ((timeRange = request.getStatTime().getNextTime(stepNum)) != null) {
            condition.setStartTime(timeRange.getKey());
            condition.setEndTime(timeRange.getValue());
            response.getProjectStatList()
                    .add(
                            new ProjectOverviewResponse.ProjectStat(
                                    new TimeRange(
                                            timeRange.getKey(),
                                            timeRange.getValue(),
                                            request.getStatTime().getStep()),
                                    this.projectMapperWrapper
                                            .getProjectMapper()
                                            .queryProjectCount(condition)));
            stepNum++;
        }
        return response;
    }

    // query the project information
    @Override
    public WeDPRResponse queryProjectByCondition(String user, ProjectRequest condition) {
        WeDPRResponse response =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        // Note: the id condition must be existed, if not query according to id condition, should
        // pass in ""
        condition.getProject().setOwner(user);
        condition.getProject().setOwnerAgency(WeDPRCommonConfig.getAgency());
        try (PageHelperWrapper pageHelperWrapper = new PageHelperWrapper(condition)) {
            List<ProjectDO> projectDOList =
                    this.projectMapperWrapper
                            .getProjectMapper()
                            .queryProject(condition.getOnlyMeta(), condition.getProject());
            response.setData(
                    new ProjectList(
                            new PageInfo<ProjectDO>(projectDOList).getTotal(), projectDOList));
        } catch (Exception e) {
            logger.warn(
                    "queryProjectByCondition exception, user: {}, condition: {}, error: ",
                    user,
                    condition.toString(),
                    e);
            response.setCode(Constant.WEDPR_FAILED);
            response.setMsg(
                    "query project information failed, user: "
                            + user
                            + ", condition: "
                            + condition.toString()
                            + ", error: "
                            + e.getMessage());
        }
        return response;
    }

    // submit a job
    @Override
    public WeDPRResponse submitJob(String user, JobRequest request) {
        WeDPRResponse response =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        try {
            request.getJob().setOwner(user);
            request.getJob().setOwnerAgency(WeDPRCommonConfig.getAgency());
            request.getJob().setTaskParties(request.getTaskParties());
            request.getJob().checkCreate();
            // check the job param
            jobChecker.checkAndParseParam(request.getJob());

            request.getJob().setStatus(JobStatus.Submitted.getStatus());
            this.projectMapperWrapper.insertJob(request.getJob());
            logger.info("submitJob, user: {}, detail: {}", user, request.getJob().toString());
            response.setData(request.getJob().getId());
        } catch (Exception e) {
            logger.warn(
                    "submitJob failed, user: {}, detail: {}, error: ",
                    user,
                    request.getJob().toString(),
                    e);
            response.setCode(Constant.WEDPR_FAILED);
            response.setMsg(
                    "submitJob for "
                            + user
                            + " failed, job detail: "
                            + request.getJob().toString()
                            + ", reason: "
                            + e.getMessage());
        }
        return response;
    }

    // query job by condition
    @Override
    public WeDPRResponse queryJobByCondition(String user, JobRequest request) {
        WeDPRResponse response =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        try (PageHelperWrapper pageHelperWrapper = new PageHelperWrapper(request)) {
            // query with using owner identity
            List<JobDO> jobDOList =
                    this.projectMapperWrapper.queryJobByCondition(
                            false, user, WeDPRCommonConfig.getAgency(), request.getJob());
            response.setData(
                    new BatchJobList(new PageInfo<JobDO>(jobDOList).getTotal(), jobDOList));
            return response;
        } catch (Exception e) {
            logger.warn(
                    "queryJobByCondition failed, user: {}, condition: {}, error: ",
                    user,
                    request.toString(),
                    e);
            response.setCode(Constant.WEDPR_FAILED);
            response.setMsg(
                    "queryJobByCondition for user "
                            + user
                            + " failed, condition: "
                            + request.getJob().toString()
                            + ", reason: "
                            + e.getMessage());
        }
        return response;
    }

    // query follower job by condition
    @Override
    public WeDPRResponse queryFollowerJobByCondition(String user, JobRequest request) {
        WeDPRResponse response =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        try (PageHelperWrapper pageHelperWrapper = new PageHelperWrapper(request)) {
            List<JobDO> jobDOList =
                    this.projectMapperWrapper
                            .getProjectMapper()
                            .queryFollowerJobByCondition(
                                    false, user, WeDPRCommonConfig.getAgency(), request.getJob());
            if (jobDOList == null) {
                return response;
            }
            // query with using owner identity
            response.setData(
                    new BatchJobList(new PageInfo<JobDO>(jobDOList).getTotal(), jobDOList));
            return response;
        } catch (Exception e) {
            logger.warn(
                    "queryFollowerJobByCondition failed, user: {}, condition: {}, error: ",
                    user,
                    request.toString(),
                    e);
            response.setCode(Constant.WEDPR_FAILED);
            response.setMsg(
                    "queryFollowerJobByCondition for user "
                            + user
                            + " failed, condition: "
                            + request.getJob().toString()
                            + ", reason: "
                            + e.getMessage());
        }
        return response;
    }

    // job retry
    @Override
    public WeDPRResponse retryJobs(String user, JobListRequest request) {
        return this.projectMapperWrapper.updateJobStatus(
                user, WeDPRCommonConfig.getAgency(), request.getJobs(), JobStatus.WaitToRetry);
    }

    // job kill
    @Override
    public WeDPRResponse killJobs(String user, JobListRequest request) {
        return this.projectMapperWrapper.updateJobStatus(
                user, WeDPRCommonConfig.getAgency(), request.getJobs(), JobStatus.WaitToKill);
    }

    @Override
    public Object queryJobOverview(String user, JobOverviewRequest jobOverviewRequest)
            throws Exception {
        JobOverviewResponse jobOverviewResponse = new JobOverviewResponse();
        jobOverviewRequest.check();
        // query the total job count
        JobDO condition = new JobDO(true);
        String agency = WeDPRCommonConfig.getAgency();
        jobOverviewResponse.setTotalCount(
                this.projectMapperWrapper.queryTotalJobCount(user, agency, condition));
        // query the count for different jobType
        for (String jobType : jobOverviewRequest.getJobTypeList()) {
            condition.setJobType(jobType);
            jobOverviewResponse
                    .getJobOverviewList()
                    .add(
                            new JobOverviewResponse.JobOverview(
                                    jobType,
                                    this.projectMapperWrapper.queryTotalJobCount(
                                            user, agency, condition)));
        }
        if (jobOverviewRequest.getStatTime() == null) {
            return jobOverviewResponse;
        }
        long stepNum = 0;
        Pair<String, String> timeRange = null;
        while ((timeRange = jobOverviewRequest.getStatTime().getNextTime(stepNum)) != null) {
            // query the jobStats by time
            condition.setJobType(null);
            condition.setStartTime(timeRange.getKey());
            condition.setEndTime(timeRange.getValue());
            JobOverviewResponse.JobStat jobStat =
                    new JobOverviewResponse.JobStat(
                            new TimeRange(
                                    timeRange.getKey(),
                                    timeRange.getValue(),
                                    jobOverviewRequest.getStatTime().getStep()),
                            this.projectMapperWrapper.queryTotalJobCount(user, agency, condition));
            for (String jobType : jobOverviewRequest.getJobTypeList()) {
                condition.setJobType(jobType);
                jobStat.getJobTypeStats()
                        .add(
                                new JobOverviewResponse.JobStat.JobTypeStat(
                                        jobType,
                                        this.projectMapperWrapper.queryTotalJobCount(
                                                user, agency, condition)));
            }
            jobOverviewResponse.getStatResults().add(jobStat);
            stepNum++;
        }
        return jobOverviewResponse;
    }
}
