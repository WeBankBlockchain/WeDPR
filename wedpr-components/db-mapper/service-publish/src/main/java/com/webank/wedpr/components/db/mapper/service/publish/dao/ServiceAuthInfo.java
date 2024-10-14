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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.webank.wedpr.common.utils.Common;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.TimeRange;
import com.webank.wedpr.components.uuid.generator.WeDPRUuidGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceAuthInfo extends TimeRange {
    private String id = WeDPRUuidGenerator.generateID();
    private String serviceId;
    private String accessKeyId;
    private String expireTime;
    private String applyTime;
    private String accessibleUser;
    private String accessibleAgency;
    private String lastUpdateTime;

    public ServiceAuthInfo(String id) {
        this.id = id;
    }

    // Note: the expireTime is timestamp format
    @JsonProperty("expired")
    public boolean expired() {
        return Common.isDateExpired(Constant.DEFAULT_TIMESTAMP_FORMAT, expireTime);
    }
}
