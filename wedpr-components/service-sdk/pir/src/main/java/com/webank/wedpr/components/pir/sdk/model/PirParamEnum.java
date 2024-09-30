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

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zachma
 * @date 2024/8/18
 */
public class PirParamEnum {
    @Getter
    @AllArgsConstructor
    public enum JobType {
        SearchValue("0"),
        SearchExist("1");
        private String value;
    }

    @Getter
    @AllArgsConstructor
    public enum AlgorithmType {
        idFilter("idFilter"),
        idObfuscation("idObfuscation");

        private String value;

        public static AlgorithmType deserialize(String value) {
            if (StringUtils.isBlank(value)) {
                return null;
            }
            for (AlgorithmType algorithmType : AlgorithmType.values()) {
                if (algorithmType.value.compareToIgnoreCase(value) == 0) {
                    return algorithmType;
                }
            }
            return null;
        }
    }

    @Getter
    @AllArgsConstructor
    public enum HttpType {
        HttpTimeout(180),
        HttpMaxRetries(1);
        private Integer value;
    }

    @Getter
    @AllArgsConstructor
    public enum JobMode {
        SDKMode(0),
        DirectorMode(1);
        private Integer value;
    }
}
