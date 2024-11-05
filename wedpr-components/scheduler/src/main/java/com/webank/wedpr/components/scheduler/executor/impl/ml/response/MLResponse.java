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

package com.webank.wedpr.components.scheduler.executor.impl.ml.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.webank.wedpr.common.utils.BaseResponse;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.components.scheduler.dag.worker.WorkerStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@ToString
public class MLResponse implements BaseResponse {

    @Data
    @ToString
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private String status;

        @JsonProperty("traffic_volume")
        private String trafficVolume;

        @JsonProperty("exec_result")
        private String execResult;

        private WorkerStatus workerStatus;

        public void setStatus(String status) {
            this.status = status;
            if (StringUtils.isNotBlank(status)) {
                this.workerStatus = WorkerStatus.deserialize(status);
            }
        }

        public void setWorkerStatus(WorkerStatus workerStatus) {
            this.workerStatus = workerStatus;
            if (this.workerStatus != null) {
                this.status = this.workerStatus.getStatus();
            }
        }
    }

    private Integer errorCode;
    private String message;
    private Result data;

    @Override
    public Boolean statusOk() {
        return errorCode.equals(Constant.WEDPR_SUCCESS);
    }

    public Boolean success() {
        if (data == null) {
            return Boolean.FALSE;
        }
        return data.getWorkerStatus().isSuccess();
    }

    public Boolean failed() {
        if (data == null) {
            return Boolean.FALSE;
        }
        return data.getWorkerStatus().isFailed();
    }

    public Boolean killed() {
        if (data == null) {
            return Boolean.FALSE;
        }
        return data.getWorkerStatus().isKilled();
    }

    public static MLResponse deserialize(String data) throws JsonProcessingException {
        return ObjectMapperFactory.getObjectMapper().readValue(data, MLResponse.class);
    }

    @Override
    public String serialize() throws Exception {
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
    }
}
