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

package com.webank.wedpr.components.task.plugin.pir.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.webank.wedpr.common.utils.Common;
import com.webank.wedpr.components.pir.sdk.core.ObfuscateData;
import com.webank.wedpr.components.pir.sdk.model.PirParamEnum;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ObfuscationParam {
    BigInteger x;
    BigInteger y;
    int index;
    PirParamEnum.AlgorithmType algorithmType;
    PirDataItem pirDataItem;

    public ObfuscationParam(ObfuscateData obfuscateData, PirParamEnum.AlgorithmType algorithmType) {
        setX(obfuscateData.getX());
        setY(obfuscateData.getY());
        setAlgorithmType(algorithmType);
    }

    public BigInteger getObfuscationValue() {
        if (algorithmType == PirParamEnum.AlgorithmType.idFilter) {
            return Common.bytesToBigInteger(
                    pirDataItem.getPirKey().getBytes(StandardCharsets.UTF_8));
        }
        return BigInteger.valueOf(index);
    }
}
