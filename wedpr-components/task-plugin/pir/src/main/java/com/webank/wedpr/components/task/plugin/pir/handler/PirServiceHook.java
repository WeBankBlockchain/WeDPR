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

package com.webank.wedpr.components.task.plugin.pir.handler;

import com.webank.wedpr.components.api.credential.core.impl.CredentialInfo;
import com.webank.wedpr.components.db.mapper.service.publish.callbacks.ServiceInvokeCallback;
import com.webank.wedpr.components.db.mapper.service.publish.dao.ServiceInvokeDO;
import com.webank.wedpr.components.db.mapper.service.publish.dao.ServiceInvokeMapper;
import com.webank.wedpr.components.db.mapper.service.publish.model.ServiceInvokeStatus;
import com.webank.wedpr.components.db.mapper.service.publish.model.ServiceType;
import com.webank.wedpr.components.hook.ServiceHook;
import com.webank.wedpr.components.pir.sdk.model.PirQueryRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PirServiceHook {
    private static final Logger logger = LoggerFactory.getLogger(PirServiceHook.class);
    private final ServiceHook serviceHook;
    private final ServiceInvokeMapper serviceInvokeMapper;

    public PirServiceHook(ServiceHook serviceHook, ServiceInvokeMapper serviceInvokeMapper) {
        this.serviceHook = serviceHook;
        this.serviceInvokeMapper = serviceInvokeMapper;
        serviceHook.registerServiceCallback(
                ServiceType.PIR.getType(), new ServiceInvokeCallback(serviceInvokeMapper));
    }

    public void onInvoke(PirQueryRequest request, ServiceInvokeStatus invokeStatus) {
        if (request == null
                || request.getQueryParam() == null
                || request.getQueryParam().getCredentialInfo() == null
                || StringUtils.isBlank(request.getQueryParam().getCredentialInfo().getUser())) {
            return;
        }
        try {

            CredentialInfo credentialInfo = request.getQueryParam().getCredentialInfo();
            serviceHook.onInvoke(
                    ServiceType.PIR.getType(),
                    new ServiceInvokeDO(
                            request.getQueryParam().getServiceId(),
                            ServiceType.PIR.getType(),
                            credentialInfo.getUser(),
                            credentialInfo.getAgency(),
                            invokeStatus.getStatus()));
        } catch (Exception e) {
            logger.warn("onInvoke exception, error: ", e);
        }
    }
}
