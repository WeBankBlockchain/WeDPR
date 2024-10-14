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

package com.webank.wedpr.components.scheduler.executor.impl.pir;

import com.webank.wedpr.common.protocol.JobType;
import com.webank.wedpr.components.pir.sdk.model.PirQueryParam;
import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.scheduler.executor.ExecutorParamChecker;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PirExecutorParamChecker implements ExecutorParamChecker {

    @Override
    public List<JobType> getJobTypeList() {
        return new ArrayList<>(Arrays.asList(JobType.PIR));
    }

    @Override
    public Object checkAndParseJob(JobDO jobDO) throws Exception {
        // deserialize the jobParam
        PirQueryParam pirQueryParam = PirQueryParam.deserialize(jobDO.getParam());
        // check the jobParam
        pirQueryParam.check(true);
        return pirQueryParam;
    }
}
