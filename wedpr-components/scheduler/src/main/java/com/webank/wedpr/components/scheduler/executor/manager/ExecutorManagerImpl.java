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

package com.webank.wedpr.components.scheduler.executor.manager;

import com.webank.wedpr.common.protocol.JobType;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.project.JobChecker;
import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.project.dao.ProjectMapperWrapper;
import com.webank.wedpr.components.scheduler.executor.ExecuteResult;
import com.webank.wedpr.components.scheduler.executor.Executor;
import com.webank.wedpr.components.scheduler.executor.callback.TaskFinishedHandler;
import com.webank.wedpr.components.scheduler.executor.impl.ExecutiveContext;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMetaBuilder;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import java.util.Map;
import java.util.concurrent.*;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutorManagerImpl implements ExecutorManager {
    private static final Logger logger = LoggerFactory.getLogger(ExecutorManagerImpl.class);
    protected Map<String, TaskFinishedHandler> handlers = new ConcurrentHashMap<>();

    protected Map<String, Executor> executors = new ConcurrentHashMap<>();

    protected Map<String, ExecutiveContext> proceedingJobs = new ConcurrentHashMap<>();
    protected ScheduledExecutorService queryStatusWorker = new ScheduledThreadPoolExecutor(1);

    private final Integer queryStatusIntervalMs;
    private final ProjectMapperWrapper projectMapperWrapper;

    public ExecutorManagerImpl(
            Integer queryStatusIntervalMs,
            FileMetaBuilder fileMetaBuilder,
            FileStorageInterface storage,
            JobChecker jobChecker,
            ProjectMapperWrapper projectMapperWrapper) {
        this.queryStatusIntervalMs = queryStatusIntervalMs;
        this.projectMapperWrapper = projectMapperWrapper;
        start();
    }

    public void start() {
        this.queryStatusWorker.scheduleAtFixedRate(
                new Runnable() {
                    @Override
                    public void run() {
                        queryAllJobStatus();
                    }
                },
                0,
                queryStatusIntervalMs,
                TimeUnit.MILLISECONDS);
    }

    protected void queryAllJobStatus() {
        for (ExecutiveContext context : proceedingJobs.values()) {
            querySingleJobStatus(context);
        }
    }

    protected void querySingleJobStatus(ExecutiveContext executiveContext) {
        String jobId = executiveContext.getJob().getId();
        try {
            Executor executor = getExecutor(executiveContext.getJob().getJobType());
            // Note: unreachable here
            if (executor == null) {
                proceedingJobs.remove(jobId);
                return;
            }

            JobDO.JobResult jobResult = executiveContext.getJob().getJobResult();
            // the job has already been finished(the sync case)
            if (jobResult != null
                    && jobResult.getJobStatus() != null
                    && jobResult.getJobStatus().finished()) {
                proceedingJobs.remove(jobId);
                return;
            }
            ExecuteResult result = executor.queryStatus(executiveContext.getTaskID());
            if (result == null) {
                return;
            }

            if (result.finished()) {
                executiveContext.onTaskFinished(result);
                proceedingJobs.remove(jobId);
            }
        } catch (Exception e) {
            logger.error(
                    "Query status for job failed, job: {}, error: ",
                    executiveContext.getJob().toString(),
                    e);
            executiveContext.onTaskFinished(
                    new ExecuteResult(
                            "Job "
                                    + executiveContext.getJob().getId()
                                    + " failed for "
                                    + e.getMessage(),
                            ExecuteResult.ResultStatus.FAILED));
            proceedingJobs.remove(jobId);
        }

        int proceedingJobsSize = proceedingJobs.size();
        if (proceedingJobsSize > 0) {
            logger.info("## query all jobs status, proceeding jobs number: {}", proceedingJobsSize);
        } else {
            logger.info("## query all jobs status, no jobs is proceeding");
        }
    }

    @Override
    public void execute(JobDO jobDO) {
        try {
            if (jobDO.getType() == null) {
                throw new WeDPRException(
                        "Invalid Job for not define the job type! job detail: " + jobDO.toString());
            }
            String jobType = jobDO.getType().getType();
            Executor executor = getExecutor(jobType);
            if (executor == null) {
                throw new WeDPRException(
                        "The executor for job with type " + jobDO.getJobType() + " is not found!");
            }
            // the subJob has already success
            if (jobDO.skipTask(jobDO.getTaskID())) {
                TaskFinishedHandler handler = getTaskFinishHandler(jobType);
                if (handler == null) {
                    return;
                }
                handler.onFinish(
                        jobDO,
                        new ExecuteResult(
                                Constant.WEDPR_SUCCESS_MSG, ExecuteResult.ResultStatus.SUCCESS));
                return;
            }
            logger.info("Begin to execute job: {}", jobDO.getId());
            proceedingJobs.put(
                    jobDO.getId(),
                    new ExecutiveContext(
                            jobDO,
                            getTaskFinishHandler(jobType),
                            jobDO.getTaskID(),
                            projectMapperWrapper));
            executor.execute(jobDO);
        } catch (Exception e) {
            logger.warn(
                    "execute failed, jobType: {}, jobId: {}, error:",
                    jobDO.getType(),
                    jobDO.getId(),
                    e);
            TaskFinishedHandler taskFinishedHandler = getTaskFinishHandler(jobDO.getJobType());
            if (taskFinishedHandler == null) {
                return;
            }
            taskFinishedHandler.onFinish(
                    jobDO,
                    new ExecuteResult(
                            "execute job failed for " + e.getMessage(),
                            ExecuteResult.ResultStatus.FAILED));
        }
    }

    @Override
    public void kill(JobDO jobDO) throws Exception {
        if (jobDO.getType() == null) {
            return;
        }
        Executor executor = getExecutor(jobDO.getJobType());
        if (executor == null) {
            return;
        }
        //// find out the proceeding job, update the status to kill
        if (this.proceedingJobs.containsKey(jobDO.getId())) {
            ExecutiveContext proceedingJobCtx = proceedingJobs.get(jobDO.getId());
            // set the job to kill status
            proceedingJobCtx.getJob().setKilled(true);
            logger.info("Remove job: {} from proceedingJobs", jobDO.getId());
            proceedingJobs.remove(jobDO.getId());
        }
        logger.info("kill job: {}", jobDO.getId());
        executor.kill(jobDO);
        logger.info("kill job: {} finished", jobDO.getId());
    }

    @Override
    public void registerOnTaskFinished(String executorType, TaskFinishedHandler finishedHandler) {
        this.handlers.put(executorType, finishedHandler);
    }

    @Override
    public TaskFinishedHandler getTaskFinishHandler(String executorType) {
        if (this.handlers.containsKey(executorType)) {
            return this.handlers.get(executorType);
        }
        return null;
    }

    @Override
    public void registerExecutor(String executorType, Executor executor) {
        executors.put(executorType, executor);
        logger.info("register executor : {}", executorType);
    }

    @SneakyThrows(Exception.class)
    @Override
    public Executor getExecutor(String jobType) {
        String executorType = JobType.getExecutorType(jobType).getType();
        if (executors.containsKey(executorType)) {
            Executor executor = executors.get(executorType);
            logger.debug("get executor, executorType: {}", executorType);
            return executor;
        }
        return null;
    }
}
