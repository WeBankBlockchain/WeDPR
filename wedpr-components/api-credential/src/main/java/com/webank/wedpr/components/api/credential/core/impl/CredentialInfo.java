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
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CredentialInfo {
    private String accessKeyID;
    private String accessSecret;
    private String nonce;
    private String timestamp;
    private String signature;

    public CredentialInfo(String accessKeyID, String accessSecret, CryptoToolkit cryptoToolkit)
            throws Exception {
        this.accessKeyID = accessKeyID;
        this.accessSecret = accessSecret;
        this.nonce = RandomStringUtils.randomNumeric(5);
        this.timestamp = String.valueOf((new Date()).getTime());
        this.signature =
                cryptoToolkit.hash(
                        cryptoToolkit.hash(accessKeyID + nonce + timestamp) + this.accessSecret);
    }
}
