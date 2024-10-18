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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.webank.wedpr.components.crypto.HashCrypto;
import com.webank.wedpr.components.crypto.config.CryptoConfig;
import com.webank.wedpr.components.crypto.impl.HashCryptoImpl;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CredentialInfo {
    private String accessKeyID;
    private String accessSecret;
    private String nonce;
    private String timestamp;
    private String signature;
    // default use SHA256_ALGORITHM
    private String hashAlgorithm = CryptoConfig.SHA256_ALGORITHM;

    @JsonIgnore private String user;
    @JsonIgnore private String agency;

    public CredentialInfo(String hashAlgorithm, String accessKeyID, String accessSecret)
            throws Exception {
        this.accessKeyID = accessKeyID;
        this.accessSecret = accessSecret;
        if (StringUtils.isNotBlank(hashAlgorithm)) {
            this.hashAlgorithm = hashAlgorithm;
        }
        this.nonce = RandomStringUtils.randomNumeric(5);
        this.timestamp = String.valueOf((new Date()).getTime());
        this.signature =
                CredentialInfo.generateSignature(
                        this.hashAlgorithm,
                        this.accessKeyID,
                        this.nonce,
                        this.timestamp,
                        this.accessSecret);
    }

    public void setHashAlgorithm(String hashAlgorithm) {
        if (StringUtils.isBlank(hashAlgorithm)) {
            return;
        }
        this.hashAlgorithm = hashAlgorithm;
    }

    public static String generateSignature(
            String hashAlgorithm,
            String accessKeyID,
            String nonce,
            String timestamp,
            String accessKeySecret)
            throws Exception {
        HashCrypto hashCrypto = new HashCryptoImpl(hashAlgorithm);
        // hash(hash(accessKeyID + nonce + timestamp) + accessKeySecret)
        return hashCrypto.hash(hashCrypto.hash(accessKeyID + nonce + timestamp) + accessKeySecret);
    }
}
