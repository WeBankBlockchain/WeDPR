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
import com.webank.wedpr.components.scheduler.executor.ExecutorParamChecker;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMetaBuilder;
import com.webank.wedpr.components.scheduler.executor.impl.psi.model.PSIJobParam;
import com.webank.wedpr.core.protocol.JobType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PSIExecutorParamChecker implements ExecutorParamChecker {

    private final FileMetaBuilder fileMetaBuilder;

    public PSIExecutorParamChecker(FileMetaBuilder fileMetaBuilder) {
        this.fileMetaBuilder = fileMetaBuilder;
    }

    @Override
    public List<JobType> getJobTypeList() {
        return new ArrayList<>(Arrays.asList(JobType.PSI));
    }

    @Override
    public Object checkAndParseJob(JobDO jobDO) throws Exception {
        // deserialize the jobParam
        PSIJobParam psiJobParam = PSIJobParam.deserialize(jobDO.getParam());
        psiJobParam.setJobID(jobDO.getId());
        psiJobParam.setDatasetIDList(jobDO.getDatasetList());
        // check the jobParam
        psiJobParam.check(this.fileMetaBuilder);
        return psiJobParam;
    }
}
