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

package com.webank.wedpr.components.scheduler.executor.impl.ml.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.webank.wedpr.common.utils.ObjectMapperFactory;

public class ModelJobResult {
    public static class ModelJobData {
        private Object jobPlanetResult;

        public Object getJobPlanetResult() {
            return jobPlanetResult;
        }

        public void setJobPlanetResult(Object jobPlanetResult) {
            this.jobPlanetResult = jobPlanetResult;
        }
    }

    private Integer errorCode;
    private String message;
    private ModelJobData data;

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

    public ModelJobData getData() {
        return data;
    }

    public void setData(ModelJobData data) {
        this.data = data;
    }

    public static ModelJobResult deserialize(String data) throws JsonProcessingException {
        return ObjectMapperFactory.getObjectMapper().readValue(data, ModelJobResult.class);
    }
}
