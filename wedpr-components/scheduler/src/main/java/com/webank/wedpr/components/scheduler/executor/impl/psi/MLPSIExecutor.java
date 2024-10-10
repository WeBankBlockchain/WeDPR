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

package com.webank.wedpr.components.scheduler.executor.impl.psi;

import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.scheduler.executor.impl.ml.model.ModelJobParam;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMetaBuilder;
import com.webank.wedpr.components.scheduler.executor.impl.psi.model.PSIJobParam;
import com.webank.wedpr.components.storage.api.FileStorageInterface;

public class MLPSIExecutor extends PSIExecutor {
    public MLPSIExecutor(FileStorageInterface storage, FileMetaBuilder fileMetaBuilder) {
        // no need to check here since MLJobParam has been checked in JobOrchestrate
        super(storage, fileMetaBuilder, null);
    }

    @Override
    public Object prepare(JobDO jobDO) throws Exception {
        // get the jobParam
        PSIJobParam psiJobParam =
                ((ModelJobParam) jobDO.getJobParam())
                        .toPSIJobParam(this.fileMetaBuilder, this.storage);
        psiJobParam.setTaskID(jobDO.getTaskID());
        preparePSIJob(jobDO, psiJobParam);
        return jobDO.getJobRequest();
    }
}
