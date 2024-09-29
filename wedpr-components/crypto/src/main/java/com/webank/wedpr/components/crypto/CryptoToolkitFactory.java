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

import com.webank.wedpr.components.crypto.config.CryptoConfig;
import com.webank.wedpr.components.crypto.impl.AESCrypto;
import com.webank.wedpr.components.crypto.impl.HashCryptoImpl;
import com.webank.wedpr.core.utils.WeDPRException;

public class CryptoToolkitFactory {
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
        return new HashCryptoImpl(CryptoConfig.getHashAlgorithmType());
    }

    public static CryptoToolkit build() throws Exception {
        return new CryptoToolkit(buildSymmetricCrypto(), buildHashCrypto());
    }
}
