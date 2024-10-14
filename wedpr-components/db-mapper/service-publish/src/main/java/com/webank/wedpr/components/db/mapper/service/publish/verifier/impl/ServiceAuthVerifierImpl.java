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

package com.webank.wedpr.components.db.mapper.service.publish.verifier.impl;

import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.api.credential.core.impl.CredentialInfo;
import com.webank.wedpr.components.db.mapper.service.publish.dao.ServiceAuthInfo;
import com.webank.wedpr.components.db.mapper.service.publish.dao.ServiceAuthMapper;
import com.webank.wedpr.components.db.mapper.service.publish.verifier.ServiceAuthVerifier;
import java.util.List;

public class ServiceAuthVerifierImpl implements ServiceAuthVerifier {
    private final ServiceAuthMapper serviceAuthMapper;

    public ServiceAuthVerifierImpl(ServiceAuthMapper serviceAuthMapper) {
        this.serviceAuthMapper = serviceAuthMapper;
    }

    @Override
    public void verify(String serviceId, CredentialInfo credentialInfo) throws Exception {
        // obtain the user information
        ServiceAuthInfo condition = new ServiceAuthInfo("");
        condition.setServiceId(serviceId);
        condition.setAccessKeyId(credentialInfo.getAccessKeyID());
        List<ServiceAuthInfo> authRecorders =
                this.serviceAuthMapper.queryServiceAuth(condition, null);
        if (authRecorders == null || authRecorders.isEmpty()) {
            throw new WeDPRException(
                    "The accessKey "
                            + credentialInfo.getAccessKeyID()
                            + " has no permission to access service "
                            + serviceId);
        }
        boolean authSuccess = false;
        ServiceAuthInfo authedInfo = null;
        for (ServiceAuthInfo serviceAuthInfo : authRecorders) {
            if (serviceAuthInfo.expired()) {
                continue;
            }
            authedInfo = serviceAuthInfo;
            break;
        }
        if (authedInfo == null) {
            throw new WeDPRException(
                    "The accessKey "
                            + credentialInfo.getAccessKeyID()
                            + " has no permission to access service "
                            + serviceId
                            + " for expiration!");
        }

        credentialInfo.setUser(authRecorders.get(0).getAccessibleUser());
        credentialInfo.setAgency(authRecorders.get(0).getAccessibleAgency());
    }
}
