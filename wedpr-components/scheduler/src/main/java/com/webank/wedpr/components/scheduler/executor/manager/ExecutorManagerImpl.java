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

import com.webank.wedpr.components.project.JobChecker;
import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.project.dao.ProjectMapperWrapper;
import com.webank.wedpr.components.scheduler.executor.ExecuteResult;
import com.webank.wedpr.components.scheduler.executor.Executor;
import com.webank.wedpr.components.scheduler.executor.callback.TaskFinishedHandler;
import com.webank.wedpr.components.scheduler.executor.impl.ExecutiveContext;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMetaBuilder;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import com.webank.wedpr.core.protocol.JobStatus;
import com.webank.wedpr.core.utils.Constant;
import com.webank.wedpr.core.utils.WeDPRException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutorManagerImpl implements ExecutorManager {
    private static final Logger logger = LoggerFactory.getLogger(ExecutorManagerImpl.class);
    protected Map<String, TaskFinishedHandler> handlers = new ConcurrentHashMap<>();

    protected Executor defaultExecutor = null;

    protected Map<String, Executor> executors = new ConcurrentHashMap<>();

    protected List<ExecutiveContext> proceedingJobs = new CopyOnWriteArrayList<>();
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
        for (ExecutiveContext context : proceedingJobs) {
            querySingleJobStatus(context);
        }
    }

    protected void querySingleJobStatus(ExecutiveContext executiveContext) {
        try {
            Executor executor = getExecutor(executiveContext.getJob().getJobType());
            // Note: unreachable here
            if (executor == null) {
                proceedingJobs.remove(executiveContext);
                return;
            }
            // the job has already been finished(the sync case)
            if (executiveContext.getJob().getJobResult().getJobStatus().finished()) {
                proceedingJobs.remove(executiveContext);
                return;
            }
            ExecuteResult result = executor.queryStatus(executiveContext.getTaskID());
            if (result.finished()) {
                executiveContext.onTaskFinished(result);
                proceedingJobs.remove(executiveContext);
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
            proceedingJobs.remove(executiveContext);
        }

        int proceedingJobsSize = proceedingJobs.size();
        if (proceedingJobsSize > 0) {
            logger.info("## query all jobs status proceeding jobs size: {}", proceedingJobsSize);
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
            proceedingJobs.add(
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
        executor.kill(jobDO);
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

        // default
        return new TaskFinishedHandler() {
            @Override
            public void onFinish(JobDO jobDO, ExecuteResult result) {
                try {
                    projectMapperWrapper.updateFinalJobResult(
                            jobDO, JobStatus.RunSuccess, result.serialize());
                } catch (Exception e) {
                    logger.error(
                            "update job status to success for job {} failed, result: {}, error: ",
                            jobDO.getId(),
                            result.toString(),
                            e);
                }
            }
        };
    }

    @Override
    public void registerExecutor(String executorType, Executor executor) {
        executors.put(executorType, executor);
        logger.info("register executor : {}", executorType);
    }

    @Override
    public void registerDefaultExecutor(Executor executor) {
        this.defaultExecutor = executor;
    }

    @Override
    public Executor getExecutor(String executorType) {
        if (executors.containsKey(executorType)) {
            Executor executor = executors.get(executorType);
            logger.debug("get executor, executorType: {}", executorType);
            return executor;
        }

        // default
        return defaultExecutor;
    }
}
