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
import lombok.ToString;
import org.apache.commons.lang3.ArrayUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtResult {
    /** @author zachma */
    @Data
    @NoArgsConstructor
    @ToString
    public static class OtResultItem {
        String pirKey;
        BigInteger e;
        BigInteger w;
        String c;

        public OtResultItem(BigInteger e, BigInteger w, String c) {
            this.e = e;
            this.w = w;
            this.c = c;
        }
    }

    private List<OtResultItem> otResultItems;

    public boolean hasNoResults() {
        if (otResultItems == null || otResultItems.isEmpty()) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "OtResult{" + "otResultItems=" + ArrayUtils.toString(otResultItems) + '}';
    }
}
