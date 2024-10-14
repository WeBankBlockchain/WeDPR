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

package com.webank.wedpr.components.db.mapper.service.publish.dao;

import com.webank.wedpr.common.utils.TimeRange;
import com.webank.wedpr.components.uuid.generator.WeDPRUuidGenerator;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ServiceInvokeDO extends TimeRange {
    private String invokeId = WeDPRUuidGenerator.generateID();
    private String serviceId;
    private String serviceType;
    private String invokeUser;
    private String invokeAgency;
    private String invokeStatus;
    private String invokeTime;
    private String lastUpdateTime;

    public ServiceInvokeDO(
            String serviceId,
            String serviceType,
            String invokeUser,
            String invokeAgency,
            String invokeStatus) {
        this.serviceId = serviceId;
        this.serviceType = serviceType;
        this.invokeUser = invokeUser;
        this.invokeAgency = invokeAgency;
        this.invokeStatus = invokeStatus;
    }

    public ServiceInvokeDO(String invokeId) {
        this.invokeId = invokeId;
    }

    public void setInvokeId(String invokeId) {
        if (invokeId == null) {
            return;
        }
        this.invokeId = invokeId;
    }
}
