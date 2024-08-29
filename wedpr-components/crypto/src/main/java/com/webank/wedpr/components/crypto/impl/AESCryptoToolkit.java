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
package com.webank.wedpr.components.crypto.impl;

import com.webank.wedpr.components.crypto.CryptoToolkit;
import com.webank.wedpr.components.crypto.config.CryptoConfig;
import com.webank.wedpr.core.utils.WeDPRException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESCryptoToolkit implements CryptoToolkit {
    private final String key;
    private final byte[] iv;
    // TODO: support more algorithms
    private static final String algorithm = "AES/GCM/NoPadding";

    public AESCryptoToolkit(String key, byte[] iv) {
        this.key = key;
        this.iv = iv;
    }

    @Override
    public String encrypt(String plaintext) throws Exception {
        try {
            byte[] contentBytes = plaintext.getBytes(StandardCharsets.UTF_8);
            Cipher cipher = Cipher.getInstance(this.algorithm);
            GCMParameterSpec params = new GCMParameterSpec(128, this.iv);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(key), params);
            return Base64.getEncoder().encodeToString(cipher.doFinal(contentBytes));
        } catch (Exception e) {
            throw new WeDPRException(e.getMessage());
        }
    }

    @Override
    public String decrypt(String ciphertext) throws Exception {
        try {
            byte[] content = Base64.getDecoder().decode(ciphertext);
            GCMParameterSpec params = new GCMParameterSpec(128, iv, 0, iv.length);
            Cipher cipher = Cipher.getInstance(this.algorithm);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(key), params);
            return new String(cipher.doFinal(content), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new WeDPRException(e.getMessage());
        }
    }

    private static SecretKeySpec getSecretKey(String key) throws NoSuchAlgorithmException {
        KeyGenerator kg = KeyGenerator.getInstance(CryptoConfig.AES_ALGORITHM);
        // 初始化密钥生成器，AES要求密钥长度为128位、192位、256位
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed(key.getBytes(StandardCharsets.UTF_8));
        kg.init(128, secureRandom);
        SecretKey secretKey = kg.generateKey();
        return new SecretKeySpec(secretKey.getEncoded(), CryptoConfig.AES_ALGORITHM);
    }
}
