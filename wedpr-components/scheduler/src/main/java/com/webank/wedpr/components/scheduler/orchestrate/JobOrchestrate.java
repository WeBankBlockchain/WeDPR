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

package com.webank.wedpr.components.scheduler.orchestrate;

import com.webank.wedpr.components.project.JobChecker;
import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.project.dao.ProjectMapperWrapper;
import com.webank.wedpr.components.scheduler.executor.ExecuteResult;
import com.webank.wedpr.components.scheduler.executor.ExecutorManager;
import com.webank.wedpr.components.scheduler.executor.callback.TaskFinishedHandler;
import com.webank.wedpr.components.scheduler.executor.impl.ml.model.ModelJobParam;
import com.webank.wedpr.components.scheduler.executor.impl.ml.request.FeatureEngineeringRequest;
import com.webank.wedpr.components.scheduler.executor.impl.ml.request.PreprocessingRequest;
import com.webank.wedpr.components.scheduler.executor.impl.ml.request.XGBJobRequest;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMetaBuilder;
import com.webank.wedpr.core.protocol.JobStatus;
import com.webank.wedpr.core.protocol.JobType;
import com.webank.wedpr.core.utils.WeDPRException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobOrchestrate {
    private static final Logger logger = LoggerFactory.getLogger(JobOrchestrate.class);
    private final ExecutorManager executorManager;
    private final ProjectMapperWrapper projectMapperWrapper;
    private JobChecker jobChecker;
    private FileMetaBuilder fileMetaBuilder;

    public JobOrchestrate(
            ExecutorManager executorManager,
            ProjectMapperWrapper projectMapperWrapper,
            JobChecker jobChecker,
            FileMetaBuilder fileMetaBuilder) {
        this.executorManager = executorManager;
        this.projectMapperWrapper = projectMapperWrapper;
        this.jobChecker = jobChecker;
        this.fileMetaBuilder = fileMetaBuilder;
        orchestrate();
    }

    public void execute(JobDO jobDO) {
        try {
            jobDO.setOriginalJobType(jobDO.getType());
            if (JobType.isPSIJob(jobDO.getJobType())) {
                this.executorManager.execute(jobDO);
                return;
            }
            if (JobType.isXGBJob(jobDO.getJobType())) {
                executeML(jobDO);
                return;
            }
            throw new WeDPRException(
                    "Unsupported jobType "
                            + jobDO.getJobType()
                            + ", jobDetail: "
                            + jobDO.toString());
        } catch (Exception e) {
            logger.warn("execute job {} failed for: {}", jobDO.getId(), e);
            onJobFailed(
                    jobDO,
                    new ExecuteResult(
                            "Execute job " + jobDO.getId() + " failed for " + e.getMessage(),
                            ExecuteResult.ResultStatus.FAILED));
        }
    }

    public ExecutorManager getExecutorManager() {
        return this.executorManager;
    }

    private void executeML(JobDO jobDO) throws Exception {
        ModelJobParam modelJobParam = (ModelJobParam) this.jobChecker.checkAndParseParam(jobDO);
        jobDO.setJobParam(modelJobParam);
        if (modelJobParam.usePSI()) {
            logger.info("executeML, run PSI, job: {}", jobDO.getId());
            jobDO.setJobType(JobType.ML_PSI.getType());
            executorManager.execute(jobDO);
        } else {
            logger.info("executeML, run preprocessing, job: {}", jobDO.getId());
            // preprocessing
            PreprocessingRequest preprocessingRequest =
                    modelJobParam.toPreprocessingRequest(fileMetaBuilder);
            jobDO.setJobRequest(preprocessingRequest);
            jobDO.setJobType(JobType.MLPreprocessing.getType());
            executorManager.execute(jobDO);
        }
    }

    private void orchestrate() {
        registerJobFinishHandler(JobType.PSI);
        executorManager.registerOnTaskFinished(
                JobType.ML_PSI.getType(),
                new TaskFinishedHandler() {
                    @Override
                    public void onFinish(JobDO jobDO, ExecuteResult result) {
                        if (result.getResultStatus().failed()) {
                            onJobFailed(jobDO, result);
                            return;
                        }
                        try {
                            // preprocessing
                            ModelJobParam modelJobParam = (ModelJobParam) jobDO.getJobParam();
                            PreprocessingRequest preprocessingRequest =
                                    modelJobParam.toPreprocessingRequest(fileMetaBuilder);
                            jobDO.setJobRequest(preprocessingRequest);
                            jobDO.setJobType(JobType.MLPreprocessing.getType());
                            executorManager.execute(jobDO);
                        } catch (Exception e) {
                            logger.warn(
                                    "ML_PSI OnTaskFinished  exception, job: {}, result: {}, error: ",
                                    jobDO.getId(),
                                    result.toString(),
                                    e);
                            onJobFailed(
                                    jobDO,
                                    new ExecuteResult(
                                            "Run job "
                                                    + jobDO.getId()
                                                    + " failed for "
                                                    + e.getMessage(),
                                            ExecuteResult.ResultStatus.FAILED));
                        }
                    }
                });
        executorManager.registerOnTaskFinished(
                JobType.MLPreprocessing.getType(),
                new TaskFinishedHandler() {
                    @Override
                    public void onFinish(JobDO jobDO, ExecuteResult result) {
                        if (result.getResultStatus().failed()) {
                            onJobFailed(jobDO, result);
                            return;
                        }
                        try {
                            if (jobDO.getOriginalJobType() != null) {
                                ModelJobParam modelJobParam = (ModelJobParam) jobDO.getJobParam();
                                jobDO.setJobType(JobType.FeatureEngineer.getType());
                                // try to execute FeatureEngineer job
                                if (executeFeatureEngineerJob(jobDO, modelJobParam)) {
                                    return;
                                }
                                // execute xgb request
                                jobDO.setType(jobDO.getOriginalJobType());
                                if (executeXGBJob(jobDO, modelJobParam)) {
                                    return;
                                }
                            }
                            onJobSuccess(jobDO, result);
                        } catch (Exception e) {
                            logger.warn(
                                    "MLPreprocessing OnTaskFinished exception, job: {}, result: {}, error: ",
                                    jobDO.getId(),
                                    result.toString(),
                                    e);
                            onJobFailed(
                                    jobDO,
                                    new ExecuteResult(
                                            "Run job "
                                                    + jobDO.getId()
                                                    + " failed for "
                                                    + e.getMessage(),
                                            ExecuteResult.ResultStatus.FAILED));
                        }
                    }
                });

        executorManager.registerOnTaskFinished(
                JobType.FeatureEngineer.getType(),
                new TaskFinishedHandler() {
                    @Override
                    public void onFinish(JobDO jobDO, ExecuteResult result) {
                        if (result.getResultStatus().failed()) {
                            onJobFailed(jobDO, result);
                        }
                        try {
                            jobDO.setType(jobDO.getOriginalJobType());
                            ModelJobParam modelJobParam = (ModelJobParam) jobDO.getJobParam();
                            if (executeXGBJob(jobDO, modelJobParam)) {
                                return;
                            }
                            onJobSuccess(jobDO, result);
                        } catch (Exception e) {
                            logger.warn(
                                    "FeatureEngineer MLTaskFinishedHandler failed, job: {}, result: {}, reason: ",
                                    jobDO.getId(),
                                    result.toString(),
                                    e);
                            onJobFailed(
                                    jobDO,
                                    new ExecuteResult(
                                            "Run job "
                                                    + jobDO.getId()
                                                    + " failed for "
                                                    + e.getMessage(),
                                            ExecuteResult.ResultStatus.FAILED));
                        }
                    }
                });
        registerJobFinishHandler(JobType.XGB_TRAIN);
        registerJobFinishHandler(JobType.XGB_PREDICT);
    }

    private boolean executeFeatureEngineerJob(JobDO jobDO, ModelJobParam modelJobParam)
            throws Exception {
        // try to execute FeatureEngineer job
        FeatureEngineeringRequest featureEngineeringRequest =
                modelJobParam.toFeatureEngineerRequest();
        if (featureEngineeringRequest != null) {
            jobDO.setJobRequest(featureEngineeringRequest);
            executorManager.execute(jobDO);
            return true;
        }
        return false;
    }

    private boolean executeXGBJob(JobDO jobDO, ModelJobParam modelJobParam) throws Exception {
        // execute xgb request
        XGBJobRequest xgbJobRequest = modelJobParam.toXGBJobRequest();
        if (xgbJobRequest == null) {
            return false;
        }
        jobDO.setJobRequest(xgbJobRequest);
        executorManager.execute(jobDO);
        return true;
    }

    private void onJobFailed(JobDO jobDO, ExecuteResult result) {
        try {
            this.projectMapperWrapper.updateFinalJobResult(
                    jobDO, JobStatus.RunFailed, result.serialize());
        } catch (Exception e) {
            logger.error(
                    "update job status to failed for job {} failed, result: {}, error: ",
                    jobDO.getId(),
                    result.toString(),
                    e);
        }
    }

    private void onJobSuccess(JobDO jobDO, ExecuteResult result) {
        try {
            this.projectMapperWrapper.updateFinalJobResult(
                    jobDO, JobStatus.RunSuccess, result.serialize());
        } catch (Exception e) {
            logger.error(
                    "update job status to success for job {} failed, result: {}, error: ",
                    jobDO.getId(),
                    result.toString(),
                    e);
        }
    }

    private void registerJobFinishHandler(JobType jobType) {
        this.executorManager.registerOnTaskFinished(
                jobType.getType(),
                new TaskFinishedHandler() {
                    @Override
                    public void onFinish(JobDO jobDO, ExecuteResult result) {
                        if (result.getResultStatus().failed()) {
                            onJobFailed(jobDO, result);
                            return;
                        }
                        onJobSuccess(jobDO, result);
                    }
                });
    }
}
