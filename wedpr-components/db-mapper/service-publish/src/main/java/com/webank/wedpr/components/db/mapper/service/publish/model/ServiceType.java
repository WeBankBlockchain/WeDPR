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

package com.webank.wedpr.components.db.mapper.service.publish.model;

import org.apache.commons.lang3.StringUtils;

public enum ServiceType {
    PIR("PIR"),
    XGB("XGB"),
    LR("LR");

    private final String type;

    ServiceType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public static ServiceType deserialize(String type) {
        if (StringUtils.isBlank(type)) {
            return null;
        }
        for (ServiceType serviceType : ServiceType.values()) {
            if (serviceType.type.compareToIgnoreCase(type) == 0) {
                return serviceType;
            }
        }
        return null;
    }
}
