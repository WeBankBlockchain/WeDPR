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

package com.webank.wedpr.components.scheduler;

import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMeta;

public class JobDetailResponse {
    private JobDO job;
    private Object modelResultDetail;
    private FileMeta resultFileInfo;

    public JobDetailResponse() {}

    public JobDetailResponse(JobDO job) {
        this.job = job;
    }

    public JobDetailResponse(JobDO job, Object modelResultDetail) {
        this.job = job;
        this.modelResultDetail = modelResultDetail;
    }

    public JobDO getJob() {
        return job;
    }

    public void setJob(JobDO job) {
        this.job = job;
    }

    public Object getModelResultDetail() {
        return modelResultDetail;
    }

    public void setModelResultDetail(Object modelResultDetail) {
        this.modelResultDetail = modelResultDetail;
    }

    public FileMeta getResultFileInfo() {
        return resultFileInfo;
    }

    public void setResultFileInfo(FileMeta resultFileInfo) {
        this.resultFileInfo = resultFileInfo;
    }

    @Override
    public String toString() {
        return "JobDetailResponse{" + "job=" + job + ", resultFileInfo=" + resultFileInfo + '}';
    }
}
