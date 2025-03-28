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
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.common.utils.TimeRange;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.db.mapper.service.publish.model.ServiceStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublishedServiceInfo extends TimeRange {
    @Getter
    public static enum ServiceAuthStatus {
        Owner("Owner"),
        Authorized("Authorized"),
        NoPermission("NoPermission"),
        Expired("Expired");
        private final String status;

        ServiceAuthStatus(String status) {
            this.status = status;
        }
    }

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
    // enable non-owner-user-filter
    @JsonIgnore protected Boolean nonOwnerUserFilter = Boolean.FALSE;
    @JsonIgnore protected List<String> filterStatusList = new ArrayList<>();
    @JsonIgnore protected String accessibleUser;
    @JsonIgnore protected String accessibleAgency;

    protected String createTime;
    protected String lastUpdateTime;
    protected String authStatus;
    protected ServiceAuthStatus serviceAuthStatus = ServiceAuthStatus.NoPermission;
    protected List<ServiceAuthInfo> serviceAuthInfos = new ArrayList<>();

    @JsonIgnore protected ServiceStatus serviceStatus;
    @JsonIgnore protected String loginUser;

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

    public void setServiceAuthInfos(List<ServiceAuthInfo> serviceAuthInfos) {
        if (serviceAuthInfos == null) {
            return;
        }
        this.serviceAuthInfos = serviceAuthInfos;
    }

    public void appendServiceAuthInfo(ServiceAuthInfo serviceAuthInfo) {
        if (this.serviceAuthInfos.contains(serviceAuthInfo)) {
            return;
        }
        this.serviceAuthInfos.add(serviceAuthInfo);
    }

    public void resetServiceAuthStatus(String agency, String user) {
        // the owner
        if (agency.compareToIgnoreCase(getAgency()) == 0
                && user.compareToIgnoreCase(getOwner()) == 0) {
            this.serviceAuthStatus = ServiceAuthStatus.Owner;
            return;
        }
        if (this.serviceAuthInfos.isEmpty()) {
            return;
        }
        this.serviceAuthStatus = ServiceAuthStatus.Expired;
        // set to be authorized if it has at least one authorized status
        for (ServiceAuthInfo serviceAuthInfo : this.serviceAuthInfos) {
            if (!serviceAuthInfo.expired()) {
                this.serviceAuthStatus = ServiceAuthStatus.Authorized;
                break;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PublishedServiceInfo)) return false;
        PublishedServiceInfo that = (PublishedServiceInfo) o;
        return Objects.equals(serviceId, that.serviceId);
    }

    public void setNonOwnerUserFilter(Boolean nonOwnerUserFilter) {
        if (nonOwnerUserFilter == null) {
            return;
        }
        this.nonOwnerUserFilter = nonOwnerUserFilter;
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceId);
    }

    @Override
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
