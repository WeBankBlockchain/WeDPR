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

package com.webank.wedpr.site.controller;

import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.project.model.*;
import com.webank.wedpr.components.project.service.ProjectService;
import com.webank.wedpr.components.token.auth.TokenUtils;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        path = Constant.WEDPR_API_PREFIX + "/project/",
        produces = {"application/json"})
public class ProjectController {
    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    @Autowired private ProjectService projectService;

    // create a new project

    @PostMapping("/createProject")
    public WeDPRResponse createProject(
            @RequestBody ProjectRequest projectDetail, HttpServletRequest request) {
        try {
            return projectService.createProject(
                    TokenUtils.getLoginUser(request).getUsername(), projectDetail);
        } catch (Exception e) {
            logger.warn("createProject exception, error: ", e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "createProject failed for " + e.getMessage());
        }
    }

    // update the project information
    @PostMapping("/updateProject")
    public WeDPRResponse updateProject(
            @RequestBody ProjectRequest updatedProject, HttpServletRequest request) {
        try {
            return projectService.updateProject(
                    TokenUtils.getLoginUser(request).getUsername(), updatedProject);
        } catch (Exception e) {
            logger.warn("updateProject exception, error: ", e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "updateProject failed for " + e.getMessage());
        }
    }
    // delete the project
    @PostMapping("/deleteProject")
    public WeDPRResponse deleteProject(
            @RequestBody List<String> projectIDList, HttpServletRequest request) {
        try {
            return projectService.deleteProject(
                    TokenUtils.getLoginUser(request).getUsername(), projectIDList);
        } catch (Exception e) {
            logger.warn("deleteProject exception, error: ", e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "deleteProject failed for " + e.getMessage());
        }
    }
    // query the project information
    @PostMapping("/queryProjectByCondition")
    public WeDPRResponse queryProjectByCondition(
            @RequestBody ProjectRequest condition, HttpServletRequest request) {
        try {
            return projectService.queryProjectByCondition(
                    TokenUtils.getLoginUser(request).getUsername(), condition);
        } catch (Exception e) {
            logger.warn("queryProjectByCondition exception, error: ", e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "queryProjectByCondition failed for " + e.getMessage());
        }
    }

    @PostMapping("/queryProjectOverview")
    public WeDPRResponse queryProjectOverview(
            @RequestBody ProjectOverviewRequest overviewRequest, HttpServletRequest request) {
        try {
            WeDPRResponse response =
                    new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
            response.setData(
                    projectService.queryProjectOverview(
                            TokenUtils.getLoginUser(request).getUsername(), overviewRequest));
            return response;
        } catch (Exception e) {
            logger.warn("queryProjectOverview exception, error: ", e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "queryProjectOverview failed for " + e.getMessage());
        }
    }

    // submit a job
    @PostMapping("/submitJob")
    public WeDPRResponse submitJob(@RequestBody JobRequest jobRequest, HttpServletRequest request) {
        try {
            return projectService.submitJob(
                    TokenUtils.getLoginUser(request).getUsername(), jobRequest);
        } catch (Exception e) {
            logger.warn("submitJob exception, error: ", e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "submitJob failed for " + e.getMessage());
        }
    }
    // query job by condition
    @PostMapping("/queryJobByCondition")
    public WeDPRResponse queryJobByCondition(
            @RequestBody JobRequest jobRequest, HttpServletRequest request) {
        try {
            return projectService.queryJobByCondition(
                    TokenUtils.getLoginUser(request).getUsername(), jobRequest);
        } catch (Exception e) {
            logger.warn(
                    "queryJobByCondition exception, condition: {}, error: ",
                    jobRequest.toString(),
                    e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "queryJobByCondition failed for " + e.getMessage());
        }
    }

    // query job by condition
    @GetMapping("/queryJobsByDatasetID")
    public WeDPRResponse queryJobsByDatasetID(
            HttpServletRequest request,
            @RequestParam(value = "datasetID", required = true) String datasetID,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        try {
            return projectService.queryJobsByDatasetID(
                    TokenUtils.getLoginUser(request).getUsername(), datasetID, pageNum, pageSize);
        } catch (Exception e) {
            logger.warn("queryJobsByDatasetID exception, condition: {}, error: ", null, e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "queryJobsByDatasetID failed for " + e.getMessage());
        }
    }

    // query jobOverview
    @PostMapping("/queryJobOverview")
    public WeDPRResponse queryJobOverview(
            @RequestBody JobOverviewRequest jobOverviewRequest, HttpServletRequest request) {
        try {
            WeDPRResponse response =
                    new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
            response.setData(
                    projectService.queryJobOverview(
                            TokenUtils.getLoginUser(request).getUsername(), jobOverviewRequest));
            return response;
        } catch (Exception e) {
            logger.warn(
                    "queryJobOverview exception, condition: {}, error: ",
                    jobOverviewRequest.toString(),
                    e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "queryJobOverview failed for " + e.getMessage());
        }
    }

    @PostMapping("/queryFollowerJobByCondition")
    public WeDPRResponse queryFollowerJobByCondition(
            @RequestBody JobRequest jobRequest, HttpServletRequest request) {
        try {
            return projectService.queryFollowerJobByCondition(
                    TokenUtils.getLoginUser(request).getUsername(), jobRequest);
        } catch (Exception e) {
            logger.warn(
                    "queryFollowerJobByCondition exception, condition: {}, error: ",
                    jobRequest.toString(),
                    e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED,
                    "queryFollowerJobByCondition failed for " + e.getMessage());
        }
    }

    // job retry
    @PostMapping("/retryJobs")
    public WeDPRResponse retryJobs(
            @RequestBody JobListRequest retryRequest, HttpServletRequest request) {
        try {
            return projectService.retryJobs(
                    TokenUtils.getLoginUser(request).getUsername(), retryRequest);
        } catch (Exception e) {
            logger.warn("retryJobs exception, error: ", e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "retryJobs failed for " + e.getMessage());
        }
    }

    // job kill
    @PostMapping("/killJobs")
    public WeDPRResponse killJobs(
            @RequestBody JobListRequest jobListRequest, HttpServletRequest request) {
        try {
            return projectService.killJobs(
                    TokenUtils.getLoginUser(request).getUsername(), jobListRequest);
        } catch (Exception e) {
            logger.warn("killJobs exception, jobs: {}, error: ", jobListRequest.toString(), e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "killJobs failed for " + e.getMessage());
        }
    }
}
