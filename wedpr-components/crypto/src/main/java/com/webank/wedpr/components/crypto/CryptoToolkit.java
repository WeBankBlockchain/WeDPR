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

import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;

public class CryptoToolkit {
    private final SymmetricCrypto symmetricCrypto;
    private final HashCrypto hashCrypto;
    private final CryptoSuite cryptoSuite;

    public CryptoToolkit(
            SymmetricCrypto symmetricCrypto, HashCrypto hashCrypto, CryptoSuite cryptoSuite) {
        this.symmetricCrypto = symmetricCrypto;
        this.hashCrypto = hashCrypto;
        this.cryptoSuite = cryptoSuite;
    }

    public String encrypt(String plain) throws Exception {
        return this.symmetricCrypto.encrypt(plain);
    }

    public String decrypt(String cipher) throws Exception {
        return this.symmetricCrypto.decrypt(cipher);
    }

    public String hash(String input) throws Exception {
        return this.hashCrypto.hash(input);
    }

    public String getHexPublicKey(String hexPrivateKey) {
        String hexPubKey = cryptoSuite.loadKeyPair(hexPrivateKey).getHexPublicKey();
        if (hexPubKey.startsWith("04")) {
            return hexPubKey;
        }
        return "04" + hexPubKey;
    }
}
