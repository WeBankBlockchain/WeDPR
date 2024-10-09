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

package com.webank.wedpr.components.loadbalancer;

import com.webank.wedpr.core.utils.Common;
import com.webank.wedpr.core.utils.Constant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntryPointInfo {
    private String serviceName;
    private String entryPoint;

    public EntryPointInfo(String entryPoint) {
        this.entryPoint = entryPoint;
    }

    public String getUrl(String uriPath) {
        if (uriPath.startsWith(Constant.URI_SPLITER)) {
            return Common.getUrl(serviceName + uriPath);
        }
        return Common.getUrl(serviceName + Constant.URI_SPLITER + uriPath);
    }

    public static List<EntryPointInfo> toEntryPointInfo(List<String> entryPointsList) {
        List<EntryPointInfo> result = new ArrayList<>();
        for (String entryPoint : entryPointsList) {
            result.add(new EntryPointInfo(entryPoint));
        }
        return result;
    }
}
