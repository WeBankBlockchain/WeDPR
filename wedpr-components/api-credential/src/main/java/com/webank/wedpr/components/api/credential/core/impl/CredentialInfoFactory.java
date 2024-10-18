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

import com.webank.wedpr.components.crypto.config.CryptoConfig;

public class CredentialInfoFactory {
    private final String accessKeyID;
    private final String accessSecret;

    public CredentialInfoFactory(String accessKeyID, String accessSecret) {
        this.accessKeyID = accessKeyID;
        this.accessSecret = accessSecret;
    }

    public CredentialInfo build(String hashAlgorithm) throws Exception {
        return new CredentialInfo(hashAlgorithm, this.accessKeyID, this.accessSecret);
    }

    public CredentialInfo build() throws Exception {
        return build(CryptoConfig.SHA256_ALGORITHM);
    }
}
