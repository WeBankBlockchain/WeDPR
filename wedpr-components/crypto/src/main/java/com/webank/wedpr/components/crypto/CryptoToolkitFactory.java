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

import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.crypto.config.CryptoConfig;
import com.webank.wedpr.components.crypto.impl.AESCrypto;
import com.webank.wedpr.components.crypto.impl.HashCryptoImpl;
import java.security.SecureRandom;
import java.util.Base64;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.model.CryptoType;

public class CryptoToolkitFactory {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final HashCrypto sha256HashCrypto =
            CryptoToolkitFactory.buildHashCrypto(CryptoConfig.SHA256_ALGORITHM);

    public static SymmetricCrypto buildSymmetricCrypto() throws WeDPRException {
        if (CryptoConfig.getSymmetricAlgorithmType().compareToIgnoreCase(CryptoConfig.AES_ALGORITHM)
                == 0) {
            return new AESCrypto(
                    CryptoConfig.getSymmetricAlgorithmKey(),
                    CryptoConfig.getSymmetricAlgorithmIv());
        }
        throw new WeDPRException(
                "Not supported symmetric algorithm: " + CryptoConfig.getSymmetricAlgorithmType());
    }

    public static SymmetricCrypto buildAESSymmetricCrypto(String key, byte[] iv) {
        return new AESCrypto(key, iv);
    }

    public static HashCrypto buildHashCrypto() {
        return CryptoToolkitFactory.buildHashCrypto(CryptoConfig.getHashAlgorithmType());
    }

    public static HashCrypto buildHashCrypto(String algorithmType) {
        return new HashCryptoImpl(algorithmType);
    }

    public static CryptoSuite buildCryptoSuite(int cryptoType) {
        return new CryptoSuite(cryptoType);
    }

    // default use sm2 crypto suite
    public static CryptoSuite buildCryptoSuite() {
        return new CryptoSuite(CryptoType.SM_TYPE);
    }

    public static CryptoToolkit build() throws Exception {
        return new CryptoToolkit(buildSymmetricCrypto(), buildHashCrypto(), buildCryptoSuite());
    }

    public static String hash(String input) throws Exception {
        return sha256HashCrypto.hash(input);
    }

    public static String generateRandomKey() {
        // 随机生成 16 位字符串格式的密钥
        byte[] keyBytes = new byte[16];
        SECURE_RANDOM.nextBytes(keyBytes);
        return Base64.getEncoder().encodeToString(keyBytes);
    }
}
