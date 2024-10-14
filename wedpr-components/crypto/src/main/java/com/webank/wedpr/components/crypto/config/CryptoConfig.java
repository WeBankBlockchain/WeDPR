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
package com.webank.wedpr.components.crypto.config;

import com.webank.wedpr.common.config.WeDPRConfig;

public class CryptoConfig {
    public static final String AES_ALGORITHM = "AES";
    public static final String SHA256_ALGORITHM = "SHA-256";

    private static String SYMMETRIC_ALGORITHM_TYPE =
            WeDPRConfig.apply("wedpr.crypto.symmetric.algorithm", AES_ALGORITHM);
    private static String HASH_ALGORITHM_TYPE =
            WeDPRConfig.apply("wedpr.crypto.hash.algorithm", SHA256_ALGORITHM);

    private static String SYMMETRIC_ALGORITHM_KEY =
            WeDPRConfig.apply("wedpr.crypto.symmetric.key", null);
    private static String SYMMETRIC_ALGORITHM_IV =
            WeDPRConfig.apply("wedpr.crypto.symmetric.iv", null);

    public static String getSymmetricAlgorithmType() {
        return SYMMETRIC_ALGORITHM_TYPE;
    }

    public static String getSymmetricAlgorithmKey() {
        return SYMMETRIC_ALGORITHM_KEY;
    }

    public static byte[] getSymmetricAlgorithmIv() {
        return SYMMETRIC_ALGORITHM_IV.getBytes();
    }

    public static String getHashAlgorithmType() {
        return HASH_ALGORITHM_TYPE;
    }
}
