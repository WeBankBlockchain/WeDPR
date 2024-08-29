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

package com.webank.wedpr.components.crypto;

import com.webank.wedpr.components.api.credential.core.CredentialStatus;
import com.webank.wedpr.components.api.credential.dao.ApiCredentialDO;
import com.webank.wedpr.core.utils.Common;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class CredentialToolkit {
    private final CryptoToolkit cryptoToolkit;

    public CredentialToolkit(CryptoToolkit cryptoToolkit) {
        this.cryptoToolkit = cryptoToolkit;
    }

    /**
     * init the credential
     *
     * @param owner the owner
     * @param credential the credential need to be intialized
     * @throws Exception throws when failed
     */
    public void initCredential(String owner, ApiCredentialDO credential) throws Exception {
        credential.setOwner(owner);
        // default disable
        if (credential.getCredentialStatus() == null) {
            credential.setCredentialStatus(CredentialStatus.Disable);
        }
        // set the accessID
        credential.setAccessID(Common.generateRandomKey());
        // set the accessSecret(encrypted)
        credential.setAccessSecret(cryptoToolkit.encrypt(Common.generateRandomKey()));
    }

    /**
     * decrypt the credential
     *
     * @param credentialDO the credential need to decrypt
     * @throws Exception throws when failed
     */
    public void decryptCredential(ApiCredentialDO credentialDO) throws Exception {
        if (credentialDO == null) {
            return;
        }
        if (StringUtils.isBlank(credentialDO.getAccessSecret())) {
            return;
        }
        credentialDO.setAccessSecret(cryptoToolkit.decrypt(credentialDO.getAccessSecret()));
    }

    public void decryptCredentials(List<ApiCredentialDO> credentialDOList) throws Exception {
        for (ApiCredentialDO credentialDO : credentialDOList) {
            decryptCredential(credentialDO);
        }
    }
}
