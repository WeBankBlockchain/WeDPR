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

package com.webank.wedpr.components.scheduler.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class JobDetailRequest {
    private String jobID;
    private Boolean fetchJobDetail = Boolean.TRUE;
    private Boolean fetchJobResult = Boolean.FALSE;
    private Boolean fetchLog = Boolean.FALSE;

    public void setFetchJobDetail(Boolean fetchJobDetail) {
        if (fetchJobDetail == null) {
            return;
        }
        this.fetchJobDetail = fetchJobDetail;
    }

    public void setFetchJobResult(Boolean fetchJobResult) {
        if (fetchJobResult == null) {
            return;
        }
        this.fetchJobResult = fetchJobResult;
    }

    public void setFetchLog(Boolean fetchLog) {
        if (fetchLog == null) {
            return;
        }
        this.fetchLog = fetchLog;
    }
}
