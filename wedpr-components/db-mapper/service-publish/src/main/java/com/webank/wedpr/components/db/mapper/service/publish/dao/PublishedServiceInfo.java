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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.webank.wedpr.components.db.mapper.service.publish.model.ServiceStatus;
import com.webank.wedpr.core.utils.ObjectMapperFactory;
import com.webank.wedpr.core.utils.TimeRange;
import com.webank.wedpr.core.utils.WeDPRException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublishedServiceInfo extends TimeRange {
    protected String serviceId;
    protected String serviceName;
    protected String serviceDesc;
    protected String serviceType;
    protected String serviceConfig;
    protected Integer syncStatus;
    protected String owner;
    protected String agency;
    protected String status;
    protected String statusMsg;
    protected String createTime;
    protected String lastUpdateTime;

    @JsonIgnore protected ServiceStatus serviceStatus;

    public PublishedServiceInfo(String serviceId) {
        this.serviceId = serviceId;
    }

    public void setServiceStatus(ServiceStatus serviceStatus) {
        this.serviceStatus = serviceStatus;
        if (this.serviceStatus != null) {
            this.status = serviceStatus.getStatus();
        }
    }

    public void setStatus(String status) {
        this.status = status;
        this.serviceStatus = ServiceStatus.deserialize(status);
    }

    public String serialize() throws Exception {
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
    }

    public static PublishedServiceInfo deserialize(String data) throws Exception {
        if (StringUtils.isBlank(data)) {
            throw new WeDPRException("Invalid empty publish request!");
        }
        return ObjectMapperFactory.getObjectMapper().readValue(data, PublishedServiceInfo.class);
    }
}
