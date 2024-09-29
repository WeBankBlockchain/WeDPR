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

import com.webank.wedpr.core.utils.WeDPRException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** @author zachma */
@Data
@EqualsAndHashCode(callSuper = true)
public class PirJobParam extends PirBaseRequest {

    private Integer filterLength = 4;

    private List<String> searchIdList;

    private ServiceConfigBody serviceConfigBody;

    public void check() throws WeDPRException {
        if (Arrays.stream(PIRParamEnum.JobType.values())
                .noneMatch(enumValue -> enumValue.getValue().equals(getJobType()))) {
            throw new WeDPRException(-1, "jobType输入错误");
        }

        if (Arrays.stream(PIRParamEnum.AlgorithmType.values())
                .noneMatch(enumValue -> enumValue.getValue().equals(getJobAlgorithmType()))) {
            throw new WeDPRException(-1, "jobAlgorithmType输入错误");
        }

        if (Objects.isNull(searchIdList) || searchIdList.size() == 0) {
            throw new WeDPRException(-1, "searchId列表不能为空");
        }
        if (getPirInvokeType().equals(PIRParamEnum.JobMode.DirectorMode.getValue())
                && Objects.isNull(serviceConfigBody)) {
            throw new WeDPRException(-1, "向导模式下serviceConfigBody不能为空");
        }
    }
}
