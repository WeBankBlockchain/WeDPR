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

import com.webank.wedpr.components.db.mapper.service.publish.dao.PublishedServiceInfo;
import com.webank.wedpr.components.hook.ServiceHook;
import com.webank.wedpr.components.http.client.HttpClientImpl;
import com.webank.wedpr.components.loadbalancer.EntryPointInfo;
import com.webank.wedpr.components.loadbalancer.LoadBalancer;
import com.webank.wedpr.components.publish.config.ServicePublisherConfig;
import com.webank.wedpr.core.utils.BaseResponse;
import com.webank.wedpr.core.utils.BaseResponseFactory;
import com.webank.wedpr.core.utils.WeDPRException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

public class PirServicePublishCallback implements ServiceHook.ServiceCallback {
    private static final Logger logger = LoggerFactory.getLogger(PirServicePublishCallback.class);

    private final LoadBalancer loadBalancer;
    private final BaseResponseFactory responseFactory;

    public PirServicePublishCallback(
            LoadBalancer loadBalancer, BaseResponseFactory responseFactory) {
        this.loadBalancer = loadBalancer;
        this.responseFactory = responseFactory;
    }

    @Override
    public void onPublish(Object serviceInfo) throws Exception {
        PublishedServiceInfo publishedServiceInfo = (PublishedServiceInfo) serviceInfo;
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
        logger.info(
                "onPublish success, service: {}, target: {}",
                publishedServiceInfo.getServiceId(),
                url);
    }
}
