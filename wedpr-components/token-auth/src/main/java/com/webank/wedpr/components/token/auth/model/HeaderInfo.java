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

package com.webank.wedpr.components.token.auth.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.webank.wedpr.common.utils.Common;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.components.token.auth.utils.SecurityUtils;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HeaderInfo {
    private String alg = SecurityUtils.HMAC_HS256;
    private String typ = Constant.DEFAULT_TOKEN_TYPE;

    public HeaderInfo() {}

    public HeaderInfo(String algorithm, String type) {
        this.alg = algorithm;
        this.typ = type;
    }

    public String getAlg() {
        return alg;
    }

    public void setAlg(String alg) {
        this.alg = alg;
    }

    public String getTyp() {
        return typ;
    }

    public void setTyp(String typ) {
        this.typ = typ;
    }

    public Map<String, Object> toDict() throws IllegalAccessException {
        return Common.objectToMap(this);
    }
}
