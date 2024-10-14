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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.webank.wedpr.common.utils.BaseResponse;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.components.scheduler.executor.impl.ml.MLExecutorConfig;

public class MLResponse implements BaseResponse {
    public static class Result {
        private String status;

        @JsonProperty("traffic_volume")
        private String trafficVolume;

        @JsonProperty("time_costs")
        private String timeCosts;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getTrafficVolume() {
            return trafficVolume;
        }

        public void setTrafficVolume(String trafficVolume) {
            this.trafficVolume = trafficVolume;
        }

        public String getTimeCosts() {
            return timeCosts;
        }

        public void setTimeCosts(String timeCosts) {
            this.timeCosts = timeCosts;
        }

        @Override
        public String toString() {
            return "Result{"
                    + "status='"
                    + status
                    + '\''
                    + ", trafficVolume='"
                    + trafficVolume
                    + '\''
                    + ", timeCosts='"
                    + timeCosts
                    + '\''
                    + '}';
        }
    }

    private Integer errorCode;
    private String message;
    private Result data;

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Result getData() {
        return data;
    }

    public void setData(Result data) {
        this.data = data;
    }

    @Override
    public Boolean statusOk() {
        return errorCode.equals(Constant.WEDPR_SUCCESS);
    }

    public Boolean success() {
        if (data == null) {
            return Boolean.FALSE;
        }
        return data.getStatus().compareToIgnoreCase(MLExecutorConfig.getSuccessStatus()) == 0;
    }

    public Boolean failed() {
        if (data == null) {
            return Boolean.FALSE;
        }
        return data.getStatus().equals(MLExecutorConfig.getFailedStatus());
    }

    public static MLResponse deserialize(String data) throws JsonProcessingException {
        return ObjectMapperFactory.getObjectMapper().readValue(data, MLResponse.class);
    }

    @Override
    public String serialize() throws Exception {
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
    }

    @Override
    public String toString() {
        return "MLResponse{"
                + "errorCode="
                + errorCode
                + ", message='"
                + message
                + '\''
                + ", data="
                + data
                + '}';
    }
}
