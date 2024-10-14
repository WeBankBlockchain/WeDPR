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

package com.webank.wedpr.components.scheduler.executor.impl.psi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.http.client.JsonRpcClient;
import com.webank.wedpr.components.http.client.model.JsonRpcResponse;
import com.webank.wedpr.components.project.JobChecker;
import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.scheduler.executor.ExecuteResult;
import com.webank.wedpr.components.scheduler.executor.Executor;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMetaBuilder;
import com.webank.wedpr.components.scheduler.executor.impl.psi.model.PSIJobParam;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PSIExecutor implements Executor {
    private static class QueryTaskParam {
        private String taskID;

        public QueryTaskParam() {}

        public QueryTaskParam(String taskID) {
            this.taskID = taskID;
        }

        public String getTaskID() {
            return taskID;
        }

        public void setTaskID(String taskID) {
            this.taskID = taskID;
        }

        public String serialize() throws JsonProcessingException {
            return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(PSIExecutor.class);

    private static final String RUN_FINISHED_STATUS = "COMPLETED";
    protected final JsonRpcClient jsonRpcClient;
    protected final FileStorageInterface storage;
    protected final FileMetaBuilder fileMetaBuilder;
    protected final JobChecker jobChecker;

    public PSIExecutor(
            FileStorageInterface storage, FileMetaBuilder fileMetaBuilder, JobChecker jobChecker) {
        this.jsonRpcClient =
                new JsonRpcClient(
                        PSIExecutorConfig.getPsiUrl(),
                        PSIExecutorConfig.getMaxTotalConnection(),
                        PSIExecutorConfig.buildConfig());
        this.storage = storage;
        this.fileMetaBuilder = fileMetaBuilder;
        this.jobChecker = jobChecker;
    }

    @Override
    public Object prepare(JobDO jobDO) throws Exception {
        // deserialize the jobParam
        PSIJobParam psiJobParam = (PSIJobParam) this.jobChecker.checkAndParseParam(jobDO);
        psiJobParam.setTaskID(jobDO.getTaskID());
        preparePSIJob(jobDO, psiJobParam);
        return jobDO.getJobRequest();
    }

    protected void preparePSIJob(JobDO jobDO, PSIJobParam psiJobParam) throws Exception {
        // download and prepare the psi file
        psiJobParam.prepare(this.fileMetaBuilder, storage);
        // convert to PSIRequest
        jobDO.setJobRequest(psiJobParam.convert(jobDO.getOwnerAgency()));
    }

    @Override
    public void execute(JobDO jobDO) throws Exception {
        prepare(jobDO);

        Object jobRequest = jobDO.getJobRequest();

        logger.info(
                "begin to submit job to PSI node, jobID: {}, taskID: {}, jobRequest: {}",
                jobDO.getId(),
                jobDO.getTaskID(),
                jobRequest);

        JsonRpcResponse response =
                this.jsonRpcClient.post(
                        PSIExecutorConfig.getPsiToken(),
                        PSIExecutorConfig.getPsiRunTaskMethod(),
                        jobDO.getJobRequest());
        if (response.statusOk()) {
            logger.info(
                    "submit PSI job to node success, jobID: {}, taskID: {}, jobRequest: {}",
                    jobDO.getId(),
                    jobDO.getTaskID(),
                    jobRequest);
            return;
        }
        if (response.getResult().getCode().equals(Constant.DuplicatedTaskCode)) {
            logger.info(
                    "PSI job has already been submitted, jobID: {}, taskID: {}, jobRequest: {}",
                    jobDO.getId(),
                    jobDO.getTaskID(),
                    jobRequest);
            return;
        }
        logger.warn(
                "Run PSI job failed, jobDetail: {}, jobRequest: {}, result: {}",
                jobDO,
                jobRequest,
                response.getResult().toString());
        throw new WeDPRException(
                "Run PSI job "
                        + jobDO.getTaskID()
                        + " failed for "
                        + response.getResult().getMessage());
    }

    @Override
    public void kill(JobDO jobDO) throws Exception {
        logger.warn("PSI not support kill! jobDetail: {}", jobDO.toString());
    }

    @SneakyThrows(Exception.class)
    private ExecuteResult parseStatusResponse(String jobID, JsonRpcResponse response) {
        if (!response.statusOk()) {
            logger.warn("queryStatus error, job: {}, response: {}", jobID, response);
            return new ExecuteResult(response.serialize(), ExecuteResult.ResultStatus.FAILED);
        }
        if (response.getResult().getStatus().compareToIgnoreCase(RUN_FINISHED_STATUS) == 0) {
            logger.info(
                    "queryStatus, job execute success, job: {}, response: {}",
                    jobID,
                    response.toString());
            return new ExecuteResult(
                    response.getResult().serialize(), ExecuteResult.ResultStatus.SUCCESS);
        }
        return new ExecuteResult(ExecuteResult.ResultStatus.RUNNING);
    }

    @Override
    public ExecuteResult queryStatus(String jobID) throws Exception {
        JsonRpcResponse response =
                this.jsonRpcClient.post(
                        PSIExecutorConfig.getPsiToken(),
                        PSIExecutorConfig.getPsiGetTaskStatusMethod(),
                        new QueryTaskParam(jobID));
        return parseStatusResponse(jobID, response);
    }
}
