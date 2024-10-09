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

package com.webank.wedpr.components.publish.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.wedpr.components.authorization.dao.AuthorizationDO;
import com.webank.wedpr.components.db.mapper.service.publish.dao.ServiceAuthInfo;
import com.webank.wedpr.core.utils.ObjectMapperFactory;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceAuthContent {
    private String serviceId;
    private String serviceName;
    private String owner;
    private String agency;
    private String accessKeyID;
    private String authTime;
    private String serviceType;

    public static List<ServiceAuthInfo> toServiceAuthInfo(AuthorizationDO authorizationDO)
            throws Exception {
        List<ServiceAuthContent> serviceAuthContents =
                ObjectMapperFactory.getObjectMapper()
                        .readValue(
                                authorizationDO.getApplyContent(),
                                new TypeReference<List<ServiceAuthContent>>() {});
        List<ServiceAuthInfo> serviceAuthInfos = new ArrayList<>();
        for (ServiceAuthContent authContent : serviceAuthContents) {
            ServiceAuthInfo serviceAuthInfo = new ServiceAuthInfo();
            serviceAuthInfo.setServiceId(authContent.getServiceId());
            serviceAuthInfo.setAccessibleUser(authorizationDO.getApplicant());
            serviceAuthInfo.setAccessibleAgency(authorizationDO.getApplicantAgency());
            serviceAuthInfo.setAccessKeyId(authContent.getAccessKeyID());
            serviceAuthInfo.setExpireTime(authContent.getAuthTime());
            serviceAuthInfos.add(serviceAuthInfo);
        }
        return serviceAuthInfos;
    }
}
