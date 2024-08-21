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

package com.webank.wedpr.components.scheduler;

import com.webank.wedpr.components.project.JobChecker;
import com.webank.wedpr.components.project.dao.ProjectMapperWrapper;
import com.webank.wedpr.components.scheduler.core.SchedulerImpl;
import com.webank.wedpr.components.scheduler.core.SchedulerTaskImpl;
import com.webank.wedpr.components.scheduler.executor.ExecutorManager;
import com.webank.wedpr.components.scheduler.executor.impl.ExecutorManagerImpl;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMetaBuilder;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import com.webank.wedpr.components.sync.ResourceSyncer;
import com.webank.wedpr.core.config.WeDPRCommonConfig;
import com.webank.wedpr.core.utils.ThreadPoolService;
import com.webank.wedpr.core.utils.WeDPRException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedulerBuilder {
    private static final Logger logger = LoggerFactory.getLogger(SchedulerBuilder.class);
    private static final String workerName = "scheduler";

    public static SchedulerTaskImpl build(
            ProjectMapperWrapper projectMapperWrapper,
            FileStorageInterface storage,
            ResourceSyncer resourceSyncer,
            FileMetaBuilder fileMetaBuilder,
            JobChecker jobChecker)
            throws Exception {
        try {
            logger.info("create SchedulerTask");
            ThreadPoolService schedulerWorker =
                    new ThreadPoolService(workerName, SchedulerTaskConfig.getWorkerQueueSize());
            // create and start the executorManager
            ExecutorManager executorManager =
                    new ExecutorManagerImpl(
                            SchedulerTaskConfig.getQueryJobStatusIntervalMs(),
                            fileMetaBuilder,
                            storage,
                            jobChecker,
                            projectMapperWrapper);
            ;
            SchedulerImpl schedulerImpl =
                    new SchedulerImpl(
                            WeDPRCommonConfig.getAgency(),
                            executorManager,
                            schedulerWorker,
                            projectMapperWrapper,
                            jobChecker,
                            fileMetaBuilder);
            SchedulerTaskImpl scheduler =
                    new SchedulerTaskImpl(projectMapperWrapper, resourceSyncer, schedulerImpl);
            logger.info("create SchedulerTask success");
            return scheduler;
        } catch (Exception e) {
            logger.error("create SchedulerTask failed, error: ", e);
            throw new WeDPRException("Create SchedulerTask failed for " + e.getMessage(), e);
        }
    }
}
