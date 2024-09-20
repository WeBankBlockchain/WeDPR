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

package com.webank.wedpr.components.scheduler.executor.impl.ml;

import com.webank.wedpr.components.http.client.HttpClientImpl;
import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.scheduler.executor.ExecuteResult;
import com.webank.wedpr.components.scheduler.executor.Executor;
import com.webank.wedpr.components.scheduler.executor.impl.ml.request.ModelJobRequest;
import com.webank.wedpr.components.scheduler.executor.impl.ml.response.MLResponse;
import com.webank.wedpr.components.scheduler.executor.impl.ml.response.MLResponseFactory;
import com.webank.wedpr.core.utils.BaseResponse;
import com.webank.wedpr.core.utils.WeDPRException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MLExecutor implements Executor {
    private static final Logger logger = LoggerFactory.getLogger(MLExecutor.class);

    public MLExecutor() {}

    @Override
    public void execute(JobDO jobDO) throws Exception {
        ModelJobRequest modelJobRequest = (ModelJobRequest) jobDO.getJobRequest();
        modelJobRequest.setTaskID(jobDO.getTaskID());
        HttpClientImpl httpClient =
                new HttpClientImpl(
                        MLExecutorConfig.getRunTaskApiUrl(jobDO.getTaskID()),
                        MLExecutorConfig.getMaxTotalConnection(),
                        MLExecutorConfig.buildConfig(),
                        new MLResponseFactory());
        logger.info("execute job: {}", jobDO.toString());
        httpClient.executePost(modelJobRequest);
    }

    @Override
    public void prepare(JobDO jobDO) throws Exception {}

    @Override
    public void kill(JobDO jobDO) throws Exception {
        logger.info("kill job: {}", jobDO.toString());
        // TODO: model node support kill by jobID
        HttpClientImpl httpClient =
                new HttpClientImpl(
                        MLExecutorConfig.getRunTaskApiUrl(jobDO.getId()),
                        MLExecutorConfig.getMaxTotalConnection(),
                        MLExecutorConfig.buildConfig(),
                        new MLResponseFactory());
        BaseResponse response = httpClient.execute(httpClient.getUrl(), true);
        if (response.statusOk()) {
            logger.info("kill job success: {}", jobDO.getJobRequest());
            return;
        }
        logger.error("kill job {} failed, response: {}", jobDO.getId(), response.serialize());
        throw new WeDPRException(response.serialize());
    }

    @Override
    public ExecuteResult queryStatus(String taskID) throws Exception {
        logger.info("query job status for {}", taskID);
        HttpClientImpl httpClient =
                new HttpClientImpl(
                        MLExecutorConfig.getRunTaskApiUrl(taskID),
                        MLExecutorConfig.getMaxTotalConnection(),
                        MLExecutorConfig.buildConfig(),
                        new MLResponseFactory());
        BaseResponse response = httpClient.execute(httpClient.getUrl(), false);
        if (!response.statusOk()) {
            logger.error(
                    "query job status for {} failed, response: {}", taskID, response.toString());
            return new ExecuteResult(response.serialize(), ExecuteResult.ResultStatus.FAILED);
        }
        MLResponse mlResponse = (MLResponse) response;
        if (mlResponse.success()) {
            return new ExecuteResult(mlResponse.serialize(), ExecuteResult.ResultStatus.SUCCESS);
        }
        if (mlResponse.failed()) {
            return new ExecuteResult(mlResponse.serialize(), ExecuteResult.ResultStatus.FAILED);
        }
        return new ExecuteResult(mlResponse.serialize(), ExecuteResult.ResultStatus.RUNNING);
    }
}
