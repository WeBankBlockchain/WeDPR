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

import com.webank.wedpr.common.config.WeDPRCommonConfig;
import com.webank.wedpr.common.protocol.JobStatus;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.api.credential.core.impl.CredentialInfo;
import com.webank.wedpr.components.api.credential.dao.ApiCredentialDO;
import com.webank.wedpr.components.api.credential.dao.ApiCredentialMapper;
import com.webank.wedpr.components.crypto.CryptoToolkit;
import com.webank.wedpr.components.crypto.config.CryptoConfig;
import com.webank.wedpr.components.db.mapper.service.publish.dao.ServiceAuthInfo;
import com.webank.wedpr.components.db.mapper.service.publish.dao.ServiceAuthMapper;
import com.webank.wedpr.components.pir.sdk.PirSDK;
import com.webank.wedpr.components.pir.sdk.config.PirSDKConfig;
import com.webank.wedpr.components.pir.sdk.impl.PirSDKImpl;
import com.webank.wedpr.components.pir.sdk.model.PirQueryParam;
import com.webank.wedpr.components.pir.sdk.model.PirResult;
import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.scheduler.executor.ExecuteResult;
import com.webank.wedpr.components.scheduler.executor.Executor;
import com.webank.wedpr.components.scheduler.executor.callback.TaskFinishedHandler;
import com.webank.wedpr.components.scheduler.executor.impl.ExecutiveContext;
import com.webank.wedpr.components.scheduler.executor.impl.ExecutiveContextBuilder;
import com.webank.wedpr.components.scheduler.executor.impl.ExecutorConfig;
import com.webank.wedpr.components.scheduler.executor.impl.helper.ExecutorHelper;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMeta;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMetaBuilder;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import com.webank.wedpr.sdk.jni.transport.WeDPRTransport;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PirExecutor implements Executor {
    private static final Logger logger = LoggerFactory.getLogger(PirExecutor.class);
    private final PirSDK pirSDK;
    private final PirExecutorParamChecker jobChecker = new PirExecutorParamChecker();
    private final ExecutiveContextBuilder executiveContextBuilder;
    private final TaskFinishedHandler taskFinishedHandler;
    private final FileStorageInterface storage;
    private final FileMetaBuilder fileMetaBuilder;
    private final ServiceAuthMapper serviceAuthMapper;
    private final ApiCredentialMapper apiCredentialMapper;
    private final CryptoToolkit cryptoToolkit;

    public PirExecutor(
            WeDPRTransport transport,
            FileStorageInterface storage,
            FileMetaBuilder fileMetaBuilder,
            ExecutiveContextBuilder executiveContextBuilder,
            TaskFinishedHandler taskFinishedHandler,
            ServiceAuthMapper serviceAuthMapper,
            ApiCredentialMapper apiCredentialMapper,
            CryptoToolkit cryptoToolkit) {
        logger.info("init the pir executor");
        this.pirSDK = new PirSDKImpl(transport);
        this.storage = storage;
        this.fileMetaBuilder = fileMetaBuilder;
        this.executiveContextBuilder = executiveContextBuilder;
        this.taskFinishedHandler = taskFinishedHandler;
        this.serviceAuthMapper = serviceAuthMapper;
        this.apiCredentialMapper = apiCredentialMapper;
        this.cryptoToolkit = cryptoToolkit;
    }

    // Note: no need to prepare
    @Override
    public Object prepare(JobDO jobDO) throws Exception {
        return this.jobChecker.checkAndParseJob(jobDO);
    }

    public CredentialInfo generateCredentialInfo(JobDO jobDO, PirQueryParam pirQueryParam)
            throws Exception {
        // obtain the accessKeyID
        ServiceAuthInfo condition = new ServiceAuthInfo("");
        condition.setAccessibleUser(jobDO.getOwner());
        condition.setAccessibleAgency(jobDO.getOwnerAgency());
        condition.setServiceId(pirQueryParam.getServiceId());
        List<ServiceAuthInfo> accessKeyList =
                this.serviceAuthMapper.queryServiceAuth(condition, null);
        if (accessKeyList == null || accessKeyList.isEmpty()) {
            logger.warn(
                    "generateCredentialInfo failed for no permission, user: {}, agency: {}, service: {}",
                    jobDO.getOwner(),
                    jobDO.getOwnerAgency(),
                    pirQueryParam.getServiceId());
            throw new WeDPRException(
                    "The user "
                            + jobDO.getOwner()
                            + " of agency "
                            + jobDO.getOwnerAgency()
                            + " can't access service "
                            + pirQueryParam.getServiceId());
        }
        // random select on accessKey
        ApiCredentialDO accessKeyCondition = new ApiCredentialDO("");
        accessKeyCondition.setAccessKeyID(accessKeyList.get(0).getAccessKeyId());
        List<ApiCredentialDO> result =
                this.apiCredentialMapper.queryCredentials(accessKeyCondition);
        if (result == null || result.isEmpty()) {
            logger.warn(
                    "generateCredentialInfo failed for the user {} has no accessKeyID: {}",
                    jobDO.getOwner(),
                    accessKeyList.get(0).getAccessKeyId());
            throw new WeDPRException(
                    "The user "
                            + jobDO.getOwner()
                            + " of agency "
                            + jobDO.getOwnerAgency()
                            + " can't access service "
                            + pirQueryParam.getServiceId()
                            + " for no corresponding accessKeyID");
        }
        // use sha256 hash algorithm
        return new CredentialInfo(
                CryptoConfig.SHA256_ALGORITHM,
                result.get(0).getAccessKeyID(),
                result.get(0).getAccessKeySecret());
    }

    @Override
    public void execute(JobDO jobDO) throws Exception {
        // Note: execute this in the threadPool
        PirSDKConfig.getThreadPoolService()
                .getThreadPool()
                .execute(
                        new Runnable() {
                            @Override
                            public void run() {
                                ExecutiveContext executiveContext =
                                        executiveContextBuilder.build(
                                                jobDO, taskFinishedHandler, jobDO.getId());
                                try {
                                    PirQueryParam queryParam = (PirQueryParam) prepare(jobDO);
                                    logger.info(
                                            "Execute the pir query task, jobID: {}", jobDO.getId());
                                    Pair<WeDPRResponse, PirResult> result =
                                            pirSDK.query(
                                                    generateCredentialInfo(jobDO, queryParam),
                                                    queryParam);
                                    ExecuteResult executeResult = new ExecuteResult();
                                    if (result.getLeft() == null || !result.getLeft().statusOk()) {
                                        executeResult.setResultStatus(
                                                ExecuteResult.ResultStatus.FAILED);
                                        // the error information
                                        executeResult.setMsg(result.getLeft().getMsg());
                                        jobDO.getJobResult().setJobStatus(JobStatus.RunFailed);
                                    } else {
                                        executeResult.setResultStatus(
                                                ExecuteResult.ResultStatus.SUCCESS);
                                        jobDO.getJobResult().setJobStatus(JobStatus.RunSuccess);
                                    }
                                    logger.info(
                                            "Execute the pir query task finished, jobID: {}",
                                            jobDO.getId());
                                    logger.info(
                                            "Execute the pir query task finished, jobID: {}, result: {}",
                                            jobDO.getId(),
                                            ObjectMapperFactory.getObjectMapper()
                                                    .writeValueAsString(result.getRight()));
                                    // store the result
                                    storePirResult(
                                            jobDO,
                                            result.getRight(),
                                            executiveContext,
                                            executeResult);
                                } catch (Exception e) {
                                    logger.warn(
                                            "Execute pir job {} failed, error: ", jobDO.getId(), e);
                                    ExecuteResult executeResult = new ExecuteResult();
                                    executeResult.setResultStatus(
                                            ExecuteResult.ResultStatus.FAILED);
                                    executeResult.setMsg(
                                            "execute pir job "
                                                    + jobDO.getId()
                                                    + " failed for "
                                                    + e.getMessage());
                                    jobDO.getJobResult().setJobStatus(JobStatus.RunFailed);
                                    executiveContext.onTaskFinished(executeResult);
                                }
                            }
                        });
    }

    protected void storePirResult(
            JobDO jobDO,
            PirResult pirResult,
            ExecutiveContext executiveContext,
            ExecuteResult executeResult)
            throws Exception {
        String localFilePath = ExecutorConfig.getPirJobResultPath(jobDO.getOwner(), jobDO.getId());
        logger.info("storePirResult for job: {}, localFilePath: {}", jobDO.getId(), localFilePath);
        boolean result = pirResult.persistentResult(localFilePath);
        if (!result) {
            executiveContext.onTaskFinished(executeResult);
            return;
        }
        // upload the result, if the userGroup is not, default set group to the hdfs superUserGroup
        String remoteFilePath =
                ExecutorHelper.uploadJobResult(
                        storage,
                        localFilePath,
                        null,
                        jobDO.getOwner(),
                        jobDO.getJobType(),
                        jobDO.getId(),
                        ExecutorConfig.getPirResultFileName(),
                        false);
        logger.info(
                "storePirResult for job {} success, localPath: {}, remotePath: {}",
                jobDO.getId(),
                localFilePath,
                remoteFilePath);
        FileMeta fileMeta =
                fileMetaBuilder.build(
                        storage.type(),
                        remoteFilePath,
                        jobDO.getOwner(),
                        WeDPRCommonConfig.getAgency());
        jobDO.getJobResult().setResult(fileMeta.serialize());
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
