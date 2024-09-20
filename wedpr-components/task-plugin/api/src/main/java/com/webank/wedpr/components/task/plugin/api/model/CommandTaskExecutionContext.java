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
package com.webank.wedpr.components.task.plugin.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.webank.wedpr.core.protocol.task.TaskExecutionContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommandTaskExecutionContext extends TaskExecutionContext {

    // the appName
    private String appName;

    // the task executePath
    private String executePath = CommandTaskConfig.getDefaultExecuteDir();
    // the logPath
    private String logPath = CommandTaskConfig.getLogPath(appName);
    // the processInformation path
    private String processInfoPath;

    // the environmentConfig
    private String environmentConfig;
    // the executeUser
    private String executeUser;

    // the cpu-quota
    private Integer cpuQuota = null;
    // the max memory
    private Integer maxMemory = null;

    // use sudo or not
    private Boolean useSudo = Boolean.FALSE;
    // running in background or not
    private Boolean runningInBackground = Boolean.FALSE;

    // transient
    @JsonIgnore private transient Process process;
    @JsonIgnore private transient int processId;
}
