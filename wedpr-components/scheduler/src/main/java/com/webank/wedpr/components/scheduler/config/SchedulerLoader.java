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

import com.webank.wedpr.components.project.JobChecker;
import com.webank.wedpr.components.project.dao.ProjectMapperWrapper;
import com.webank.wedpr.components.scheduler.SchedulerBuilder;
import com.webank.wedpr.components.scheduler.client.SchedulerClient;
import com.webank.wedpr.components.scheduler.core.SchedulerTaskImpl;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMetaBuilder;
import com.webank.wedpr.components.scheduler.executor.impl.pir.PirExecutor;
import com.webank.wedpr.components.scheduler.executor.impl.remote.RemoteSchedulerExecutor;
import com.webank.wedpr.components.scheduler.executor.manager.ExecutorManager;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import com.webank.wedpr.components.storage.builder.StoragePathBuilder;
import com.webank.wedpr.components.storage.config.HdfsStorageConfig;
import com.webank.wedpr.components.storage.config.LocalStorageConfig;
import com.webank.wedpr.components.sync.ResourceSyncer;
import com.webank.wedpr.components.sync.config.ResourceSyncerConfig;
import com.webank.wedpr.core.protocol.ExecutorType;
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

    @Bean(name = "schedulerTaskImpl")
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    @ConditionalOnMissingBean
    public SchedulerTaskImpl schedulerTaskImpl() throws Exception {
        FileMetaBuilder fileMetaBuilder =
                new FileMetaBuilder(new StoragePathBuilder(hdfsConfig, localStorageConfig));
        SchedulerTaskImpl schedulerTask =
                SchedulerBuilder.buildSchedulerTask(
                        projectMapperWrapper, storage, resourceSyncer, fileMetaBuilder, jobChecker);
        // register the executors
        if (schedulerTask.getScheduler().getExecutorManager() != null) {
            logger.info("Register Executor for the ExecutorManager");
            registerExecutors(schedulerTask.getScheduler().getExecutorManager(), fileMetaBuilder);
        }
        schedulerTask.start();
        return schedulerTask;
    }

    protected void registerExecutors(
            ExecutorManager executorManager, FileMetaBuilder fileMetaBuilder) {

        SchedulerClient schedulerClient = new SchedulerClient();
        RemoteSchedulerExecutor remoteSchedulerExecutor =
                new RemoteSchedulerExecutor(schedulerClient, jobChecker, storage, fileMetaBuilder);
        executorManager.registerExecutor(ExecutorType.REMOTE.getType(), remoteSchedulerExecutor);

        /*
        // register the executor
        PSIExecutor psiExecutor = new PSIExecutor(storage, fileMetaBuilder, jobChecker);
        executorManager.registerExecutor(JobType.PSI.getType(), psiExecutor);

        logger.info("register PSIExecutor success");
        MLPSIExecutor mlpsiExecutor = new MLPSIExecutor(storage, fileMetaBuilder);
        executorManager.registerExecutor(JobType.ML_PSI.getType(), mlpsiExecutor);

        logger.info("register ML-PSIExecutor success");
        MLExecutor mlExecutor = new MLExecutor();
        executorManager.registerExecutor(JobType.MLPreprocessing.getType(), mlExecutor);
        executorManager.registerExecutor(JobType.FeatureEngineer.getType(), mlExecutor);
        executorManager.registerExecutor(JobType.XGB_TRAIN.getType(), mlExecutor);
        executorManager.registerExecutor(JobType.XGB_PREDICT.getType(), mlExecutor);
        logger.info("register MLExecutor success");
        */

        // register the pir executor, TODO: implement the taskFinishHandler
        executorManager.registerExecutor(
                ExecutorType.PIR.getType(), new PirExecutor(weDPRTransport, null));
        logger.info("register PirExecutor success");
    }
}
