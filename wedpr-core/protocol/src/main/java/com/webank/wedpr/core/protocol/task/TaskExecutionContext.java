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
package com.webank.wedpr.core.protocol.task;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.webank.wedpr.core.utils.BaseRequest;
import com.webank.wedpr.core.utils.ObjectMapperFactory;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskExecutionContext implements BaseRequest {
    private static final long serialVersionUID = -1L;

    // the taskID
    protected String taskID;
    // the workflowID of the task
    protected String workflowID;
    // the taskType
    protected String taskType;
    protected String taskParameters;

    // used to var-substitution
    private Map<String, String> parameterMap;

    // the startTime
    protected Long startTime = System.currentTimeMillis();
    // -1 means never timeout
    protected Long taskTimeoutMs = -1L;

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        if (startTime == null || startTime == 0) {
            return;
        }
        this.startTime = startTime;
    }

    @Override
    public String serialize() throws Exception {
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
    }
}
