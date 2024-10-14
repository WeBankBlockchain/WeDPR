/*
 * Copyright 2017-2025 [webank-wedpr]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 */
package com.webank.wedpr.common.protocol;

import org.apache.commons.lang3.StringUtils;

public enum RequestAuthType {
    JWT("JWT"),
    ApiSignature("ApiSignature");

    private final String type;

    RequestAuthType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public static RequestAuthType deserialize(String type) {
        // default is JWT
        if (StringUtils.isBlank(type)) {
            return JWT;
        }
        for (RequestAuthType requestAuthType : RequestAuthType.values()) {
            if (requestAuthType.type.compareToIgnoreCase(type) == 0) {
                return requestAuthType;
            }
        }
        return JWT;
    }
}
