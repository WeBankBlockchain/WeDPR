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

package com.webank.wedpr.components.scheduler.executor.impl.ml.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.webank.wedpr.common.protocol.JobType;
import com.webank.wedpr.components.scheduler.executor.impl.model.AlgorithmType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = false)
public class PreprocessingRequest extends ModelJobRequest {

    @JsonProperty("need_run_psi")
    private Boolean needIntersection;

    public PreprocessingRequest() {}

    public PreprocessingRequest(ModelJobRequest modelJobRequest, AlgorithmType algorithmType) {
        super(modelJobRequest);
        this.algorithmType = algorithmType.getType();
        this.needIntersection = modelJobRequest.getBaseModelSetting().getUsePsi();
        this.setTaskType(JobType.MLPreprocessing.getType());
    }
}
