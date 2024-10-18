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
package com.webank.wedpr.components.api.credential.core.impl;

import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.api.credential.core.CredentialCache;
import com.webank.wedpr.components.api.credential.core.CredentialVerifier;
import com.webank.wedpr.components.api.credential.dao.ApiCredentialDO;
import javax.servlet.http.HttpServletRequest;

public class CredentialVerifierImpl implements CredentialVerifier {
    private final CredentialCache credentialCache;

    public CredentialVerifierImpl(CredentialCache credentialCache) {
        this.credentialCache = credentialCache;
    }

    @Override
    public CredentialInfo verify(CredentialInfo credentialInfo) throws Exception {
        ApiSignature apiSignature = new ApiSignature(credentialInfo);
        // verify the signature
        if (!apiSignature.verifySignature(credentialInfo.getAccessSecret())) {
            throw new WeDPRException("access forbidden for invalid signature!");
        }
        return credentialInfo;
    }

    @Override
    public ApiCredentialDO verify(HttpServletRequest request) throws Exception {
        ApiSignature apiSignature = new ApiSignature(request);
        // obtain the accessKeySecret by accessKeyID
        ApiCredentialDO apiCredentialDO =
                credentialCache.getAccessKey(apiSignature.getAccessKeyID());
        if (apiCredentialDO == null) {
            throw new WeDPRException(
                    "api signature verify failed for unauthorized accessKey: "
                            + apiSignature.getAccessKeyID());
        }
        if (apiCredentialDO.getCredentialStatus().isDisabled()) {
            throw new WeDPRException(
                    "access forbidden for disabled accessKey: " + apiSignature.getAccessKeyID());
        }
        // verify the signature
        if (!apiSignature.verifySignature(apiCredentialDO.getAccessKeySecret())) {
            throw new WeDPRException("access forbidden for invalid signature!");
        }
        return new ApiCredentialDO(
                apiSignature.getAccessKeyID(),
                apiCredentialDO.getOwner(),
                apiCredentialDO.getStatus());
    }
}
