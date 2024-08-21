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

package com.webank.wedpr.components.scheduler.executor.impl;

import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.project.dao.ProjectMapperWrapper;
import com.webank.wedpr.components.scheduler.executor.ExecuteResult;
import com.webank.wedpr.components.scheduler.executor.callback.TaskFinishedHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutiveContext {
    private static final Logger logger = LoggerFactory.getLogger(ExecutiveContext.class);
    private final JobDO job;

    private final TaskFinishedHandler taskFinishedHandler;
    private final ProjectMapperWrapper projectMapperWrapper;
    private final String taskID;
    private final Long startTime;

    public ExecutiveContext(
            JobDO job,
            TaskFinishedHandler taskFinishedHandler,
            String taskID,
            ProjectMapperWrapper projectMapperWrapper) {
        this.job = job;
        this.taskFinishedHandler = taskFinishedHandler;
        this.taskID = taskID;
        this.projectMapperWrapper = projectMapperWrapper;
        this.startTime = System.currentTimeMillis();
    }

    public JobDO getJob() {
        return this.job;
    }

    public TaskFinishedHandler getTaskFinishedHandler() {
        return this.taskFinishedHandler;
    }

    public void onTaskFinished(ExecuteResult result) {
        JobDO.JobResultItem subJobResult =
                new JobDO.JobResultItem(
                        taskID, result.getResultStatus().success(), result.serialize());
        subJobResult.setStartTime(this.startTime);
        subJobResult.setTimeCostMs(System.currentTimeMillis() - this.startTime);
        // record the sub-task result
        job.updateSubJobResult(subJobResult);

        this.projectMapperWrapper.updateJobResult(job.getId(), null, job.getResult());
        logger.info(
                "onTaskFinished, job: {}, task: {}, type: {}, result: {}",
                job.getId(),
                taskID,
                job.getType(),
                result.serialize());
        if (this.taskFinishedHandler == null) {
            return;
        }
        this.taskFinishedHandler.onFinish(job, result);
    }

    public String getTaskID() {
        return this.taskID;
    }
}
