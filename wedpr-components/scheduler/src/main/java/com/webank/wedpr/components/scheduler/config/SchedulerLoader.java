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

package com.webank.wedpr.components.scheduler.config;

import static com.webank.wedpr.components.scheduler.SchedulerBuilder.WORKER_NAME;

import com.webank.wedpr.common.protocol.ExecutorType;
import com.webank.wedpr.common.protocol.JobStatus;
import com.webank.wedpr.common.utils.ThreadPoolService;
import com.webank.wedpr.components.api.credential.dao.ApiCredentialMapper;
import com.webank.wedpr.components.crypto.CryptoToolkitFactory;
import com.webank.wedpr.components.db.mapper.dataset.mapper.DatasetMapper;
import com.webank.wedpr.components.db.mapper.service.publish.dao.ServiceAuthMapper;
import com.webank.wedpr.components.loadbalancer.LoadBalancer;
import com.webank.wedpr.components.project.JobChecker;
import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.project.dao.ProjectMapperWrapper;
import com.webank.wedpr.components.scheduler.SchedulerBuilder;
import com.webank.wedpr.components.scheduler.core.SchedulerTaskImpl;
import com.webank.wedpr.components.scheduler.core.SpdzConnections;
import com.webank.wedpr.components.scheduler.executor.ExecuteResult;
import com.webank.wedpr.components.scheduler.executor.callback.TaskFinishedHandler;
import com.webank.wedpr.components.scheduler.executor.impl.ExecutiveContextBuilder;
import com.webank.wedpr.components.scheduler.executor.impl.dag.DagSchedulerExecutor;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMetaBuilder;
import com.webank.wedpr.components.scheduler.executor.impl.pir.PirExecutor;
import com.webank.wedpr.components.scheduler.executor.manager.ExecutorManager;
import com.webank.wedpr.components.scheduler.mapper.JobWorkerMapper;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import com.webank.wedpr.components.storage.builder.StoragePathBuilder;
import com.webank.wedpr.components.storage.config.HdfsStorageConfig;
import com.webank.wedpr.components.storage.config.LocalStorageConfig;
import com.webank.wedpr.components.sync.ResourceSyncer;
import com.webank.wedpr.components.sync.config.ResourceSyncerConfig;
import com.webank.wedpr.sdk.jni.transport.WeDPRTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@AutoConfigureAfter({ResourceSyncerConfig.class, JobCheckerConfig.class})
public class SchedulerLoader {
    private static final Logger logger = LoggerFactory.getLogger(SchedulerLoader.class);
    @Autowired private ProjectMapperWrapper projectMapperWrapper;
    @Autowired private JobWorkerMapper jobWorkerMapper;

    @Autowired
    @Qualifier("loadBalancer")
    private LoadBalancer loadBalancer;

    @Autowired private LocalStorageConfig localStorageConfig;
    @Autowired private HdfsStorageConfig hdfsConfig;

    @Qualifier("fileStorage")
    @Autowired
    private FileStorageInterface storage;

    @Autowired private ResourceSyncer resourceSyncer;
    @Autowired private JobChecker jobChecker;

    @Qualifier("weDPRTransport")
    @Autowired
    private WeDPRTransport weDPRTransport;

    @Qualifier("spdzConnections")
    @Autowired
    private SpdzConnections spdzConnections;

    @Autowired private ServiceAuthMapper serviceAuthMapper;
    @Autowired private ApiCredentialMapper apiCredentialMapper;
    @Autowired private DatasetMapper datasetMapper;

    @Bean(name = "schedulerTaskImpl")
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    @ConditionalOnMissingBean
    public SchedulerTaskImpl schedulerTaskImpl() throws Exception {
        FileMetaBuilder fileMetaBuilder =
                new FileMetaBuilder(new StoragePathBuilder(hdfsConfig, localStorageConfig));

        ThreadPoolService schedulerWorker =
                new ThreadPoolService(WORKER_NAME, SchedulerTaskConfig.getWorkerQueueSize());

        SchedulerTaskImpl schedulerTask =
                SchedulerBuilder.buildSchedulerTask(
                        projectMapperWrapper,
                        jobWorkerMapper,
                        loadBalancer,
                        storage,
                        resourceSyncer,
                        fileMetaBuilder,
                        jobChecker,
                        schedulerWorker);

        // register the executors
        if (schedulerTask.getScheduler().getExecutorManager() != null) {
            logger.info("Register Executor for the ExecutorManager");
            registerExecutors(
                    schedulerTask.getScheduler().getExecutorManager(),
                    fileMetaBuilder,
                    schedulerWorker);
        }
        schedulerTask.start();
        return schedulerTask;
    }

    protected void registerExecutors(
            ExecutorManager executorManager,
            FileMetaBuilder fileMetaBuilder,
            ThreadPoolService threadPoolService)
            throws Exception {

        DagSchedulerExecutor dagSchedulerExecutor =
                new DagSchedulerExecutor(
                        loadBalancer,
                        jobWorkerMapper,
                        jobChecker,
                        storage,
                        fileMetaBuilder,
                        executorManager,
                        new ExecutiveContextBuilder(projectMapperWrapper),
                        threadPoolService,
                        datasetMapper,
                        spdzConnections);
        executorManager.registerExecutor(ExecutorType.DAG.getType(), dagSchedulerExecutor);
        // default
        TaskFinishedHandler taskFinishedHandler =
                new TaskFinishedHandler() {
                    @Override
                    public void onFinish(JobDO jobDO, ExecuteResult result) {
                        try {
                            if (result.getResultStatus() == null
                                    || result.getResultStatus().failed()) {
                                projectMapperWrapper.updateFinalJobResult(
                                        jobDO, JobStatus.RunFailed, result.serialize());
                            } else {
                                projectMapperWrapper.updateFinalJobResult(
                                        jobDO, JobStatus.RunSuccess, result.serialize());
                            }
                        } catch (Exception e) {
                            logger.error(
                                    "update job status for job {} failed, result: {}, error: ",
                                    jobDO.getId(),
                                    result.toString(),
                                    e);
                        }
                    }
                };

        executorManager.registerOnTaskFinished(ExecutorType.DAG.getType(), taskFinishedHandler);

        // register the pir executor
        executorManager.registerExecutor(
                ExecutorType.PIR.getType(),
                new PirExecutor(
                        weDPRTransport,
                        storage,
                        new FileMetaBuilder(new StoragePathBuilder(hdfsConfig, localStorageConfig)),
                        new ExecutiveContextBuilder(projectMapperWrapper),
                        new TaskFinishedHandler() {
                            @Override
                            public void onFinish(JobDO jobDO, ExecuteResult result) {
                                projectMapperWrapper.updateJobResult(
                                        jobDO.getId(), jobDO.getJobResult().getJobStatus(), null);
                            }
                        },
                        serviceAuthMapper,
                        apiCredentialMapper,
                        CryptoToolkitFactory.build()));
        logger.info("register PirExecutor success");
    }
}
