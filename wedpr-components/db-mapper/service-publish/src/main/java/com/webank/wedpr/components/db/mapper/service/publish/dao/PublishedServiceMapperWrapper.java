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

import java.util.*;
import java.util.stream.Collectors;

public class PublishedServiceMapperWrapper {
    private final ServiceAuthMapper serviceAuthMapper;
    private final PublishedServiceMapper publishedServiceMapper;

    public PublishedServiceMapperWrapper(
            ServiceAuthMapper serviceAuthMapper, PublishedServiceMapper publishedServiceMapper) {
        this.serviceAuthMapper = serviceAuthMapper;
        this.publishedServiceMapper = publishedServiceMapper;
    }

    public List<PublishedServiceInfo> query(
            String user,
            String agency,
            String accessKeyID,
            PublishedServiceInfo condition,
            List<String> serviceIdList) {
        List<PublishedServiceInfo> result =
                this.publishedServiceMapper.queryPublishedService(condition, serviceIdList);
        if (result == null || result.isEmpty()) {
            return result;
        }
        Map<String, PublishedServiceInfo> publishedServiceInfoMap =
                result.stream()
                        .collect(
                                Collectors.toMap(
                                        PublishedServiceInfo::getServiceId,
                                        a -> a,
                                        (k1, k2) -> k1));
        List<String> selectedServiceList = new ArrayList<>(publishedServiceInfoMap.keySet());
        ServiceAuthInfo serviceAuthCondition = new ServiceAuthInfo();
        serviceAuthCondition.setAccessibleUser(user);
        serviceAuthCondition.setAccessibleAgency(agency);
        serviceAuthCondition.setAccessKeyId(accessKeyID);
        // query the auth info
        List<ServiceAuthInfo> serviceAuthInfos =
                this.serviceAuthMapper.queryServiceAuth(null, selectedServiceList);
        // merge the result
        for (ServiceAuthInfo serviceAuthInfo : serviceAuthInfos) {
            if (publishedServiceInfoMap.containsKey(serviceAuthInfo.getServiceId())) {
                publishedServiceInfoMap
                        .get(serviceAuthInfo.getServiceId())
                        .appendServiceAuthInfo(serviceAuthInfo);
            }
        }
        List<PublishedServiceInfo> queriedResult = new ArrayList<>();
        for (PublishedServiceInfo item : publishedServiceInfoMap.values()) {
            item.resetServiceAuthStatus();
            queriedResult.add(item);
        }
        return queriedResult;
    }
}
