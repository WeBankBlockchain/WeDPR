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

import com.webank.wedpr.components.crypto.CryptoToolkit;

public class CredentialInfoFactory {
    private final String accessKeyID;
    private final String accessSecret;
    private final CryptoToolkit cryptoToolkit;

    public CredentialInfoFactory(
            String accessKeyID, String accessSecret, CryptoToolkit cryptoToolkit) {
        this.accessKeyID = accessKeyID;
        this.accessSecret = accessSecret;
        this.cryptoToolkit = cryptoToolkit;
    }

    public CredentialInfo build() throws Exception {
        return new CredentialInfo(this.accessKeyID, this.accessSecret, cryptoToolkit);
    }
}
