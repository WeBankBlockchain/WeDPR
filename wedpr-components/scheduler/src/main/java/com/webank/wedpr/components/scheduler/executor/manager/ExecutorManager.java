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

import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.scheduler.executor.Executor;
import com.webank.wedpr.components.scheduler.executor.callback.TaskFinishedHandler;

public interface ExecutorManager {

    void execute(JobDO jobDO);

    void kill(JobDO jobDO) throws Exception;

    @Deprecated
    void registerOnTaskFinished(String executorType, TaskFinishedHandler finishedHandler);

    TaskFinishedHandler getTaskFinishHandler(String executorType);

    void registerExecutor(String executorType, Executor executor);

    void registerDefaultExecutor(Executor executor);

    Executor getExecutor(String executorType);
}
