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

public enum ServiceStatus {
    Publishing("Publishing"),
    PublishFailed("PublishFailed"),
    PublishSuccess("PublishSuccess");

    private final String status;

    ServiceStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }

    public boolean isReady() {
        return ordinal() == ServiceStatus.PublishSuccess.ordinal();
    }

    public static ServiceStatus deserialize(String status) {
        if (StringUtils.isBlank(status)) {
            return null;
        }
        for (ServiceStatus serviceStatus : ServiceStatus.values()) {
            if (serviceStatus.status.compareToIgnoreCase(status) == 0) {
                return serviceStatus;
            }
        }
        return null;
    }
}
