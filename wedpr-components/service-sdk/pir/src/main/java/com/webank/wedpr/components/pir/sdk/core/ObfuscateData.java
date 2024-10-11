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

package com.webank.wedpr.components.pir.sdk.core;

import java.math.BigInteger;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zachma
 * @date 2024/8/20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ObfuscateData {
    @Data
    public static class ObfuscateDataItem {
        BigInteger z0;
        String filter;
        int idIndex;
        List<String> idHashList;
    }

    BigInteger b;
    BigInteger x;
    BigInteger y;
    List<ObfuscateDataItem> obfuscateDataItems;
    // the searched fields
    String[] params;

    public ObfuscateData(
            BigInteger b, BigInteger x, BigInteger y, List<ObfuscateDataItem> obfuscateDataItems) {
        setB(b);
        setX(x);
        setY(y);
        setObfuscateDataItems(obfuscateDataItems);
    }
}
