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

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/** @author zachma */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PirResult {

    @Data
    @NoArgsConstructor
    @ToString
    public static class PirResultItem {
        String searchId;
        Boolean isExists = false;
        String value;

        public void setValue(String value) {
            this.value = value;
            if (StringUtils.isNotBlank(this.value)) {
                this.isExists = true;
            }
        }
    }

    private List<PirResultItem> pirResultItemList;

    @Override
    public String toString() {
        return "PirResult{" + "pirResultItemList=" + ArrayUtils.toString(pirResultItemList) + '}';
    }
}
