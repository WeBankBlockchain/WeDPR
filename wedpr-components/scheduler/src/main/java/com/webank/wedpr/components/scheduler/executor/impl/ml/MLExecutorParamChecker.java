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

package com.webank.wedpr.components.scheduler.executor.impl.ml;

import com.webank.wedpr.common.protocol.JobType;
import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.scheduler.executor.ExecutorParamChecker;
import com.webank.wedpr.components.scheduler.executor.impl.ml.model.ModelJobParam;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMetaBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MLExecutorParamChecker implements ExecutorParamChecker {
    private FileMetaBuilder fileMetaBuilder;

    public MLExecutorParamChecker(FileMetaBuilder fileMetaBuilder) {
        this.fileMetaBuilder = fileMetaBuilder;
    }

    @Override
    public List<JobType> getJobTypeList() {
        return new ArrayList<>(
                Arrays.asList(
                        JobType.XGB_PREDICT,
                        JobType.XGB_TRAIN,
                        JobType.LR_TRAIN,
                        JobType.LR_PREDICT));
    }

    @Override
    public Object checkAndParseJob(JobDO jobDO) throws Exception {
        ModelJobParam modelJobParam = ModelJobParam.deserialize(jobDO.getParam());
        modelJobParam.setJobID(jobDO.getId());
        modelJobParam.setJobType(jobDO.getType());
        modelJobParam.setDatasetIDList(jobDO.getDatasetList());
        // check the param
        modelJobParam.check();
        return modelJobParam;
    }
}
