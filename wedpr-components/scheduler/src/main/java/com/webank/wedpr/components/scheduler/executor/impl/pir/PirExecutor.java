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

package com.webank.wedpr.components.scheduler.executor.impl.pir;

import com.webank.wedpr.components.pir.sdk.PirSDK;
import com.webank.wedpr.components.pir.sdk.impl.PirSDKImpl;
import com.webank.wedpr.components.pir.sdk.model.PirQueryParam;
import com.webank.wedpr.components.pir.sdk.model.PirResult;
import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.scheduler.executor.ExecuteResult;
import com.webank.wedpr.components.scheduler.executor.Executor;
import com.webank.wedpr.components.scheduler.executor.callback.TaskFinishedHandler;
import com.webank.wedpr.components.scheduler.executor.impl.ExecutiveContext;
import com.webank.wedpr.components.scheduler.executor.impl.ExecutiveContextBuilder;
import com.webank.wedpr.core.utils.WeDPRResponse;
import com.webank.wedpr.sdk.jni.transport.WeDPRTransport;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PirExecutor implements Executor {
    private static final Logger logger = LoggerFactory.getLogger(PirExecutor.class);
    private final PirSDK pirSDK;
    private final PirExecutorParamChecker jobChecker = new PirExecutorParamChecker();
    private final ExecutiveContextBuilder executiveContextBuilder;
    private final TaskFinishedHandler taskFinishedHandler;

    public PirExecutor(
            WeDPRTransport transport,
            ExecutiveContextBuilder executiveContextBuilder,
            TaskFinishedHandler taskFinishedHandler) {
        logger.info("init the pir executor");
        this.pirSDK = new PirSDKImpl(transport);
        this.executiveContextBuilder = executiveContextBuilder;
        this.taskFinishedHandler = taskFinishedHandler;
    }

    // Note: no need to prepare
    @Override
    public Object prepare(JobDO jobDO) throws Exception {
        return this.jobChecker.checkAndParseJob(jobDO);
    }

    @Override
    public void execute(JobDO jobDO) throws Exception {
        ExecutiveContext executiveContext =
                executiveContextBuilder.build(jobDO, taskFinishedHandler, jobDO.getId());
        PirQueryParam queryParam = (PirQueryParam) prepare(jobDO);
        Pair<WeDPRResponse, PirResult> result = this.pirSDK.query(queryParam);
        ExecuteResult executeResult = new ExecuteResult();
        if (result.getLeft() == null || !result.getLeft().statusOk()) {
            executeResult.setResultStatus(ExecuteResult.ResultStatus.FAILED);
        } else {
            executeResult.setResultStatus(ExecuteResult.ResultStatus.SUCCESS);
        }
        // TODO: store the query PirResult
        executiveContext.onTaskFinished(executeResult);
    }

    @Override
    public void kill(JobDO jobDO) throws Exception {}

    // Note: no need to query the status
    @Override
    public ExecuteResult queryStatus(String jobID) throws Exception {
        return null;
    }
}
