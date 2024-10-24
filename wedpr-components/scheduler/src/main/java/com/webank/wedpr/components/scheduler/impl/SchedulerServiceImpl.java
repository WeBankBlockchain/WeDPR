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

package com.webank.wedpr.components.scheduler.impl;

import com.webank.wedpr.common.protocol.JobStatus;
import com.webank.wedpr.common.protocol.JobType;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.loadbalancer.LoadBalancer;
import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.project.dao.ProjectMapperWrapper;
import com.webank.wedpr.components.scheduler.JobDetailResponse;
import com.webank.wedpr.components.scheduler.SchedulerService;
import com.webank.wedpr.components.scheduler.executor.impl.ml.MLExecutorClient;
import com.webank.wedpr.components.scheduler.executor.impl.ml.model.ModelJobResult;
import com.webank.wedpr.components.scheduler.executor.impl.ml.request.GetTaskResultRequest;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMetaBuilder;
import com.webank.wedpr.components.scheduler.executor.impl.psi.model.PSIJobParam;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class SchedulerServiceImpl implements SchedulerService {
    private static final Logger logger = LoggerFactory.getLogger(SchedulerServiceImpl.class);
    @Autowired private ProjectMapperWrapper projectMapperWrapper;
    @Autowired private FileMetaBuilder fileMetaBuilder;

    @Autowired
    @Qualifier("loadBalancer")
    private LoadBalancer loadBalancer;

    @Override
    public Object queryJobDetail(String user, String agency, String jobID) throws Exception {
        List<JobDO> jobDOList = this.projectMapperWrapper.queryJobDetail(jobID, user, agency);
        if (jobDOList == null || jobDOList.isEmpty()) {
            throw new WeDPRException("queryJobDetail failed for the job " + jobID + " not exist!");
        }
        // query the jobDetail
        JobDO jobDO = jobDOList.get(0);
        // run failed, no need to fetch the result
        if (!JobStatus.success(jobDO.getStatus())) {
            return new JobDetailResponse(jobDO, null, null);
        }
        // the ml job
        if (jobDO.getType().mlJob()) {
            ModelJobResult.ModelJobData modelJobData =
                    (ModelJobResult.ModelJobData)
                            MLExecutorClient.getJobResult(
                                    loadBalancer,
                                    new GetTaskResultRequest(
                                            user, jobDO.getId(), jobDO.getJobType()));
            return new JobDetailResponse(
                    jobDO, modelJobData.getJobPlanetResult(), modelJobData.getModelData());
        }
        JobDetailResponse response = new JobDetailResponse(jobDO);
        // the psi job, parse the output
        if (JobType.isPSIJob(jobDO.getJobType())) {
            PSIJobParam psiJobParam = PSIJobParam.deserialize(jobDO.getParam());
            response.setResultFileInfo(psiJobParam.getResultPath(fileMetaBuilder, jobID));
        }
        // the pir job, get result files
        if (JobType.isPirJob(jobDO.getJobType())) {
            response.setResultFileInfo(fileMetaBuilder.build(jobDO.getJobResult().getResult()));
        }
        return response;
    }
}
