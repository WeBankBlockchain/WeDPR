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

import com.webank.wedpr.common.utils.BaseRequest;
import com.webank.wedpr.common.utils.ObjectMapperFactory;

public class GetTaskResultRequest implements BaseRequest {
    String jobID;
    String user;
    String jobType;

    public GetTaskResultRequest() {}

    public GetTaskResultRequest(String user, String jobID, String jobType) {
        this.user = user;
        this.jobID = jobID;
        this.jobType = jobType;
    }

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    @Override
    public String toString() {
        return "GetTaskResultRequest{"
                + "user='"
                + user
                + '\''
                + ", jobType='"
                + jobType
                + '\''
                + '}';
    }

    @Override
    public String serialize() throws Exception {
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
    }
}
