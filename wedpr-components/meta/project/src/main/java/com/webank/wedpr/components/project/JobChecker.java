/*
 * Copyright 2017-2025  [webank-wedpr]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.webank.wedpr.components.project;

import com.webank.wedpr.common.protocol.JobType;
import com.webank.wedpr.components.project.dao.JobDO;
import java.util.HashMap;
import java.util.Map;

public class JobChecker {

    public interface JobCheckHandler {
        public Object checkAndParseParam(JobDO jobDO) throws Exception;
    }

    private Map<String, JobCheckHandler> handlers = new HashMap<>();

    public JobChecker() {}

    public void registerJobCheckHandler(JobType jobType, JobCheckHandler handler) {
        handlers.put(jobType.getType(), handler);
    }

    public Object checkAndParseParam(JobDO jobDO) throws Exception {
        if (!handlers.containsKey(jobDO.getType().getType())) {
            return null;
        }
        return handlers.get(jobDO.getType().getType()).checkAndParseParam(jobDO);
    }
}
