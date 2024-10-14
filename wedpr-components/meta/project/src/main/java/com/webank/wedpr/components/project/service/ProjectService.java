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

package com.webank.wedpr.components.project.service;

import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.project.model.*;
import java.util.List;

public interface ProjectService {
    // create a new project
    public abstract WeDPRResponse createProject(String user, ProjectRequest projectDetail);
    // update the project information
    public abstract WeDPRResponse updateProject(String user, ProjectRequest updatedProject);
    // delete the project
    public abstract WeDPRResponse deleteProject(String user, List<String> projectIDList);
    // query the project information
    public abstract WeDPRResponse queryProjectByCondition(String user, ProjectRequest condition);

    public abstract Object queryProjectOverview(String user, ProjectOverviewRequest request)
            throws Exception;

    public abstract Object queryJobOverview(String user, JobOverviewRequest jobOverviewRequest)
            throws Exception;

    // submit a job
    public abstract WeDPRResponse submitJob(String user, JobRequest request);
    // query job by condition
    public abstract WeDPRResponse queryJobByCondition(String user, JobRequest request);
    // query job list by dataset id
    public abstract WeDPRResponse queryJobsByDatasetID(
            String user, String datasetID, Integer pageNum, Integer pageSize);

    public abstract WeDPRResponse queryFollowerJobByCondition(String user, JobRequest request);

    // job retry
    public abstract WeDPRResponse retryJobs(String user, JobListRequest request);

    // job kill
    public abstract WeDPRResponse killJobs(String user, JobListRequest request);
}
