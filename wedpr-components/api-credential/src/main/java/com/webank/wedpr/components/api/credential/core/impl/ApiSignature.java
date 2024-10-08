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
import com.webank.wedpr.core.utils.Common;
import com.webank.wedpr.core.utils.WeDPRException;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
public class ApiSignature {
    private static Logger logger = LoggerFactory.getLogger(ApiSignature.class);
    public static String ACCESS_ID_KEY = "accessKeyID";
    public static String NONCE_KEY = "nonce";
    public static String TIMESTAMP_KEY = "timestamp";
    public static String SIGNATURE_KEY = "signature";

    private String accessKeyID;
    private String nonce;
    private String timestamp;
    private String signature;

    public ApiSignature(CredentialInfo credentialInfo) throws Exception {
        this.accessKeyID = credentialInfo.getAccessKeyID();
        this.nonce = credentialInfo.getNonce();
        this.timestamp = credentialInfo.getTimestamp();
        this.signature = credentialInfo.getSignature();
    }

    public ApiSignature(HttpServletRequest request) throws Exception {
        this.accessKeyID = request.getParameter(ACCESS_ID_KEY);
        this.nonce = request.getParameter(NONCE_KEY);
        this.timestamp = request.getParameter(TIMESTAMP_KEY);
        this.signature = request.getParameter(SIGNATURE_KEY);
        // check the content
        check();
    }

    public void check() throws Exception {
        Common.requireNonEmpty("accessKeyID", accessKeyID);
        Common.requireNonEmpty("nonce", nonce);
        Common.requireNonEmpty("timestamp", timestamp);
        Common.requireNonEmpty("signature", signature);
        // check the timestamp
        long currentTime = (new Date()).getTime();
        if ((currentTime - Long.parseLong(timestamp)) / 1000
                > CredentialConfig.getSignautureExpirationTimeSeconds()) {
            throw new WeDPRException(
                    "Invalid expired apiSignature, timestamp: "
                            + timestamp
                            + ", currentTimestamp: "
                            + currentTime);
        }
    }

    public String generateSignature(CryptoToolkit cryptoToolkit, String accessKeySecret)
            throws Exception {
        // hash(hash(accessKeyID + nonce + timestamp) + accessKeySecret)
        return cryptoToolkit.hash(
                cryptoToolkit.hash(accessKeyID + nonce + timestamp) + accessKeySecret);
    }

    public boolean verifySignature(CryptoToolkit cryptoToolkit, String accessKeySecret)
            throws Exception {
        try {
            String generatedSignature = generateSignature(cryptoToolkit, accessKeySecret);
            return generatedSignature.equals(signature);
        } catch (Exception e) {
            logger.warn("verifySignature exception, accessID: {}, error: ", accessKeyID, e);
            return false;
        }
    }
}
