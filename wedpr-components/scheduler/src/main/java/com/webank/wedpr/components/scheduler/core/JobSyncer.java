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

package com.webank.wedpr.components.scheduler.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.project.dao.ProjectMapperWrapper;
import com.webank.wedpr.components.project.model.BatchJobList;
import com.webank.wedpr.components.scheduler.api.SchedulerApi;
import com.webank.wedpr.components.sync.ResourceSyncer;
import com.webank.wedpr.components.sync.core.ResourceActionRecord;
import com.webank.wedpr.components.sync.core.ResourceActionRecorderBuilder;
import com.webank.wedpr.components.uuid.generator.WeDPRUuidGenerator;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobSyncer {
    private static final Logger logger = LoggerFactory.getLogger(JobSyncer.class);

    public enum JobAction {
        RunAction("RunAction"),
        KillAction("KillAction");
        private final String action;

        JobAction(String action) {
            this.action = action;
        }

        public String getAction() {
            return this.action;
        }
    }

    public class JobCommitHandler implements ResourceSyncer.CommitHandler {
        @Override
        public void call(ResourceSyncer.CommitArgs args) throws WeDPRException {
            onReceiveResourceRecord(args);
        }
    }

    private final String agency;
    private final String resourceType;
    private final ResourceSyncer resourceSyncer;
    private final ProjectMapperWrapper projectMapperWrapper;
    private final ResourceActionRecorderBuilder resourceBuilder;
    private final SchedulerApi scheduler;

    public JobSyncer(
            String agency,
            String resourceType,
            ResourceSyncer resourceSyncer,
            SchedulerApi scheduler,
            ProjectMapperWrapper projectMapperWrapper) {
        this.agency = agency;
        this.resourceType = resourceType;
        this.projectMapperWrapper = projectMapperWrapper;
        this.resourceSyncer = resourceSyncer;
        this.resourceBuilder = new ResourceActionRecorderBuilder(this.agency, this.resourceType);
        this.resourceSyncer.registerCommitHandler(this.resourceType, new JobCommitHandler());
        this.scheduler = scheduler;
    }

    public String sync(String trigger, JobAction action, String resourceContent) {
        return syncJobResource(trigger, WeDPRUuidGenerator.generateID(), action, resourceContent);
    }

    public String syncJobResource(
            String trigger, String resourceID, JobAction action, String resourceContent) {
        ResourceActionRecord record =
                this.resourceBuilder.build(resourceID, action.getAction(), resourceContent);
        this.resourceSyncer.sync(trigger, record);
        logger.info("Sync resource, ID: {}, content: {}", resourceID, record.toString());
        return record.getResourceID();
    }

    private void onReceiveResourceRecord(ResourceSyncer.CommitArgs commitArgs) {
        if (commitArgs == null) {
            return;
        }
        try {
            if (commitArgs == null
                    || commitArgs.getResourceActionRecord() == null
                    || StringUtils.isBlank(
                            commitArgs.getResourceActionRecord().getResourceAction())) {
                logger.warn("onReceiveResourceRecord, receive invalid resourceRecord");
                return;
            }
            logger.info(
                    "JobSyncer, receive record: {}",
                    commitArgs.getResourceActionRecord().toString());

            String action = commitArgs.getResourceActionRecord().getResourceAction();
            if (action.compareToIgnoreCase(JobAction.RunAction.getAction()) == 0) {
                onReceiveRunAction(commitArgs);
            } else if (action.compareToIgnoreCase(JobAction.KillAction.getAction()) == 0) {
                onReceiveKillAction(commitArgs);
            } else {
                logger.warn(
                        "onReceiveResourceRecord, invalid action: {}, resourceID: {}",
                        action,
                        commitArgs.getResourceActionRecord().getResourceID());
            }
        } catch (Exception e) {
            logger.error(
                    "onReceiveResourceRecord exception, detail: {}, error: ",
                    commitArgs.getResourceActionRecord().toString(),
                    e);
        }
    }

    public void onReceiveRunAction(ResourceSyncer.CommitArgs commitArgs)
            throws JsonProcessingException {
        BatchJobList jobList =
                BatchJobList.deserialize(commitArgs.getResourceActionRecord().getResourceContent());
        List<JobDO> jobs = new ArrayList<>();
        for (JobDO jobDO : jobList.getJobs()) {
            if (!jobDO.isJobParty(this.agency)) {
                continue;
            }
            // update the job-status to running
            this.projectMapperWrapper.recordJobStatus(jobDO);
            jobs.add(jobDO);
        }
        logger.info("onReceiveRunAction, begin to schedule the jobs, size: {}", jobs.size());
        this.scheduler.batchRunJobs(jobs);
    }

    // receive the kill job action
    public void onReceiveKillAction(ResourceSyncer.CommitArgs commitArgs)
            throws JsonProcessingException {
        BatchJobList jobList =
                BatchJobList.deserialize(commitArgs.getResourceActionRecord().getResourceAction());
        logger.info("onReceiveKillAction, job size: {}", jobList.getJobs());
        this.scheduler.batchKillJobs(jobList.getJobs());
    }
}
