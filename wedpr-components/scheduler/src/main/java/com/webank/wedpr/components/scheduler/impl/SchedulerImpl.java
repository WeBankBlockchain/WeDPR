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
import com.webank.wedpr.common.utils.ThreadPoolService;
import com.webank.wedpr.components.project.JobChecker;
import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.project.dao.ProjectMapperWrapper;
import com.webank.wedpr.components.scheduler.api.SchedulerApi;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMetaBuilder;
import com.webank.wedpr.components.scheduler.executor.manager.ExecutorManager;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedulerImpl implements SchedulerApi {
    private static final Logger logger = LoggerFactory.getLogger(SchedulerImpl.class);
    private final ProjectMapperWrapper projectMapperWrapper;
    private final String agency;
    private final ThreadPoolService threadPoolService;
    // private final JobOrchestrate jobOrchestrate;
    private final ExecutorManager executorManager;

    public SchedulerImpl(
            String agency,
            ExecutorManager executorManager,
            ThreadPoolService threadPoolService,
            ProjectMapperWrapper projectMapperWrapper,
            JobChecker jobChecker,
            FileStorageInterface fileStorageInterface,
            FileMetaBuilder fileMetaBuilder) {
        this.agency = agency;
        //        this.jobOrchestrate =
        //                new JobOrchestrate(
        //                        executorManager, projectMapperWrapper, jobChecker,
        // fileMetaBuilder);
        this.projectMapperWrapper = projectMapperWrapper;
        this.threadPoolService = threadPoolService;
        this.executorManager = executorManager;
    }

    @Override
    public void batchKillJobs(List<JobDO> jobs) {
        for (JobDO jobDO : jobs) {
            if (!jobDO.isJobParty(this.agency)) {
                continue;
            }
            // set the job status to killing
            this.projectMapperWrapper.updateSingleJobStatus(null, null, jobDO, JobStatus.Killing);
            threadPoolService
                    .getThreadPool()
                    .execute(
                            new Runnable() {
                                @Override
                                public void run() {
                                    killJob(jobDO);
                                }
                            });
        }
    }

    @Override
    public void batchRunJobs(List<JobDO> jobs) {
        for (JobDO jobDO : jobs) {
            if (!jobDO.isJobParty(this.agency)) {
                continue;
            }

            // set the job status to running
            this.projectMapperWrapper.updateSingleJobStatus(null, null, jobDO, JobStatus.Running);
            threadPoolService
                    .getThreadPool()
                    .execute(
                            new Runnable() {
                                @Override
                                public void run() {
                                    executorManager.execute(jobDO);
                                }
                            });
        }
    }

    @Override
    public ExecutorManager getExecutorManager() {
        return executorManager;
    }

    protected void killJob(JobDO jobDO) {
        long startT = System.currentTimeMillis();
        try {
            logger.info("begin to killJob, detail: {}", jobDO.toString());
            // get the executor
            this.executorManager.kill(jobDO);
            this.projectMapperWrapper.updateSingleJobStatus(null, null, jobDO, JobStatus.Killed);
            logger.info(
                    "kill job success, id: {}, type: {}, cost: {}",
                    jobDO.getId(),
                    jobDO.getJobType(),
                    (System.currentTimeMillis() - startT));
        } catch (Exception e) {
            logger.warn(
                    "kill job failed, id: {}, type: {}, cost: {}, reason: ",
                    jobDO.getId(),
                    jobDO.getJobType(),
                    (System.currentTimeMillis() - startT),
                    e);
            JobDO updatedJob = new JobDO(jobDO.getId());
            updatedJob.setStatus(JobStatus.KillFailed.getStatus());
            updatedJob.setResult("kill job failed for: " + (e.getMessage()));
            this.projectMapperWrapper.updateJob(updatedJob);
        }
    }
}
