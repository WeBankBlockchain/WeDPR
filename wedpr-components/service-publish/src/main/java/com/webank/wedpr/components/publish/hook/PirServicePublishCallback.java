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

package com.webank.wedpr.components.publish.hook;

import com.webank.wedpr.common.utils.BaseResponse;
import com.webank.wedpr.common.utils.BaseResponseFactory;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.db.mapper.service.publish.dao.ServiceAuthInfo;
import com.webank.wedpr.components.db.mapper.service.publish.dao.ServiceAuthMapper;
import com.webank.wedpr.components.hook.ServiceHook;
import com.webank.wedpr.components.http.client.HttpClientImpl;
import com.webank.wedpr.components.loadbalancer.EntryPointInfo;
import com.webank.wedpr.components.loadbalancer.LoadBalancer;
import com.webank.wedpr.components.publish.config.ServicePublisherConfig;
import com.webank.wedpr.components.publish.entity.request.PublishCreateRequest;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

public class PirServicePublishCallback implements ServiceHook.ServiceCallback {
    private static final Logger logger = LoggerFactory.getLogger(PirServicePublishCallback.class);

    private final LoadBalancer loadBalancer;
    private final BaseResponseFactory responseFactory;
    private final ServiceAuthMapper serviceAuthMapper;

    public PirServicePublishCallback(
            LoadBalancer loadBalancer,
            BaseResponseFactory responseFactory,
            ServiceAuthMapper serviceAuthMapper) {
        this.loadBalancer = loadBalancer;
        this.responseFactory = responseFactory;
        this.serviceAuthMapper = serviceAuthMapper;
    }

    public void batchInsertServiceAuthInfo(
            String serviceId, PublishCreateRequest publishServiceInfo) {
        if (publishServiceInfo.getGrantedAccessKeyList() == null
                || publishServiceInfo.getGrantedAccessKeyList().isEmpty()) {
            return;
        }
        List<ServiceAuthInfo> serviceAuthInfoList = new ArrayList<>();
        for (String accessKeyID : publishServiceInfo.getGrantedAccessKeyList()) {
            ServiceAuthInfo serviceAuthInfo = new ServiceAuthInfo();
            serviceAuthInfo.setServiceId(serviceId);
            serviceAuthInfo.setAccessKeyId(accessKeyID);
            serviceAuthInfo.setAccessibleUser(publishServiceInfo.getOwner());
            serviceAuthInfo.setAccessibleAgency(publishServiceInfo.getAgency());
            // the empty means
            serviceAuthInfo.setExpireTime(Constant.NEVER_EXPIRE_TIMESTAMP);
            serviceAuthInfoList.add(serviceAuthInfo);
        }
        this.serviceAuthMapper.batchInsertServiceAuth(serviceAuthInfoList);
    }

    @Override
    public void onPublish(Object serviceInfo) throws Exception {
        PublishCreateRequest publishedServiceInfo = (PublishCreateRequest) serviceInfo;
        EntryPointInfo selectedEntryPoint =
                loadBalancer.selectService(
                        LoadBalancer.Policy.ROUND_ROBIN, publishedServiceInfo.getServiceType());
        if (selectedEntryPoint == null) {
            throw new WeDPRException(
                    "Publish service "
                            + publishedServiceInfo.getServiceId()
                            + " failed for not found "
                            + publishedServiceInfo.getServiceId()
                            + " service!");
        }
        String url =
                selectedEntryPoint.getUrl(ServicePublisherConfig.getPirPublishServiceUriPath());
        logger.info("onPublish, serviceInfo: {}, target: {}", publishedServiceInfo.toString(), url);
        HttpClientImpl httpClient =
                new HttpClientImpl(
                        url,
                        ServicePublisherConfig.getMaxTotalConnection(),
                        ServicePublisherConfig.buildConfig(),
                        responseFactory);
        BaseResponse response = httpClient.executePost(publishedServiceInfo, HttpStatus.OK.value());
        if (response == null) {
            throw new WeDPRException(
                    "publish service: "
                            + publishedServiceInfo.getServiceId()
                            + " for no response received!");
        }
        if (!response.statusOk()) {
            throw new WeDPRException(
                    "publish service: "
                            + publishedServiceInfo.getServiceId()
                            + " failed, response: "
                            + response.serialize());
        }
        // register auth information
        batchInsertServiceAuthInfo(publishedServiceInfo.getServiceId(), publishedServiceInfo);
        logger.info(
                "onPublish success, service: {}, target: {}",
                publishedServiceInfo.getServiceId(),
                url);
    }

    @Override
    public void onInvoke(Object serviceInvokeInfo) throws Exception {}
}
