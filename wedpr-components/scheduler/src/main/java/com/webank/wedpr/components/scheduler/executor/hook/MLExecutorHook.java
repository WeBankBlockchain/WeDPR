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

import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.scheduler.executor.impl.ml.request.ModelJobRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MLExecutorHook implements ExecutorHook {
    private static final Logger logger = LoggerFactory.getLogger(MLExecutorHook.class);

    public MLExecutorHook() {}

    @Override
    public Object prepare(JobDO jobDO) throws Exception {
        ModelJobRequest modelJobRequest = (ModelJobRequest) jobDO.getJobRequest();
        return jobDO.getJobRequest();
    }
}
