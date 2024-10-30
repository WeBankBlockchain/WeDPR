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

package com.webank.wedpr.components.scheduler.executor.hook;

import com.webank.wedpr.components.project.JobChecker;
import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMetaBuilder;
import com.webank.wedpr.components.scheduler.executor.impl.psi.model.PSIJobParam;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PSIExecutorHook implements ExecutorHook {
    private static final Logger logger = LoggerFactory.getLogger(PSIExecutorHook.class);

    protected final FileStorageInterface storage;
    protected final FileMetaBuilder fileMetaBuilder;
    protected final JobChecker jobChecker;

    public PSIExecutorHook(
            FileStorageInterface storage, FileMetaBuilder fileMetaBuilder, JobChecker jobChecker) {
        this.storage = storage;
        this.fileMetaBuilder = fileMetaBuilder;
        this.jobChecker = jobChecker;
    }

    @Override
    public Object prepare(JobDO jobDO) throws Exception {
        // deserialize the jobParam
        PSIJobParam psiJobParam = (PSIJobParam) this.jobChecker.checkAndParseParam(jobDO);
        psiJobParam.setTaskID(jobDO.getTaskID());
        preparePSIJob(jobDO, psiJobParam);
        return jobDO.getJobRequest();
    }

    protected void preparePSIJob(JobDO jobDO, PSIJobParam psiJobParam) throws Exception {
        // download and prepare the psi file
        psiJobParam.prepare(this.fileMetaBuilder, storage);
        // convert to PSIRequest
        jobDO.setJobRequest(psiJobParam.convert(jobDO.getOwnerAgency()));
    }
}
