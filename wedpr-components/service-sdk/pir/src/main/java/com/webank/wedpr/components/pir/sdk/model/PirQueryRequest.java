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

import com.webank.wedpr.components.pir.sdk.core.ObfuscateData;
import com.webank.wedpr.core.utils.ObjectMapperFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PirQueryRequest {
    private PirQueryParam queryParam;
    private ObfuscateData obfuscateData;

    public String serialize() throws Exception {
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
    }

    public static PirQueryRequest deserialize(byte[] data) throws Exception {
        if (data == null) {
            return null;
        }
        return ObjectMapperFactory.getObjectMapper().readValue(data, PirQueryRequest.class);
    }
}
