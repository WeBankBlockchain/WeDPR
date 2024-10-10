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
import com.webank.wedpr.components.scheduler.executor.impl.model.AlgorithmType;
import com.webank.wedpr.core.protocol.JobType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class XGBJobRequest extends ModelJobRequest {

    public XGBJobRequest(ModelJobRequest modelJobRequest, JobType jobType) {
        super(modelJobRequest);
        this.setTaskType(jobType.getType());
        if (jobType.trainJob()) {
            this.algorithmType = AlgorithmType.WEDPR_TRAIN.getType();
        } else {
            this.algorithmType = AlgorithmType.WEDPR_PREDICT.getType();
        }
    }
}
