package com.webank.wedpr.components.crypto;

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

public class AESHelper {
    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final String KEY_ALGORITHM = "AES";
    private static final SecureRandom secureRandom = new SecureRandom();

    public static String encrypt(String plaintext, String key) throws WeDPRException {
        try {
            byte[] iv = new byte[12];
            secureRandom.nextBytes(iv);
            byte[] contentBytes = plaintext.getBytes(StandardCharsets.UTF_8);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            GCMParameterSpec params = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(key), params);
            byte[] encryptData = cipher.doFinal(contentBytes);
            assert encryptData.length == contentBytes.length + 16;
            byte[] message = new byte[12 + contentBytes.length + 16];
            System.arraycopy(iv, 0, message, 0, 12);
            System.arraycopy(encryptData, 0, message, 12, encryptData.length);
            return Base64.getEncoder().encodeToString(message);
        } catch (Exception e) {
            throw new WeDPRException(e.getMessage());
        }
    }

    public static String decrypt(String ciphertext, String key) throws WeDPRException {
        try {
            byte[] content = Base64.getDecoder().decode(ciphertext);
            if (content.length < 12 + 16) {
                throw new IllegalArgumentException();
            }
            GCMParameterSpec params = new GCMParameterSpec(128, content, 0, 12);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(key), params);
            byte[] decryptData = cipher.doFinal(content, 12, content.length - 12);
            return new String(decryptData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new WeDPRException(e.getMessage());
        }
    }

    private static SecretKeySpec getSecretKey(String key) throws NoSuchAlgorithmException {
        KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
        // 初始化密钥生成器，AES要求密钥长度为128位、192位、256位
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed(key.getBytes(StandardCharsets.UTF_8));
        kg.init(128, secureRandom);
        SecretKey secretKey = kg.generateKey();
        return new SecretKeySpec(secretKey.getEncoded(), KEY_ALGORITHM);
    }
}
