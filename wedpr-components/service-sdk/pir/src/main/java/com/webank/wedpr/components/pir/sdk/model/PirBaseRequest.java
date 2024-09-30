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

package com.webank.wedpr.components.pir.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class PirBaseRequest {
    String jobType;
    // 数据方机构id
    // 匿踪算法类型(0: hash披露算法, 1: hash混淆算法)
    String jobAlgorithmType;
    @JsonIgnore PirParamEnum.AlgorithmType algorithmType;
    // 查询范围
    Integer obfuscationOrder = 9;

    Integer pirInvokeType = PirParamEnum.JobMode.SDKMode.getValue();

    public void setJobAlgorithmType(String jobAlgorithmType) {
        this.jobAlgorithmType = jobAlgorithmType;
        this.algorithmType = PirParamEnum.AlgorithmType.deserialize(jobAlgorithmType);
    }
}
