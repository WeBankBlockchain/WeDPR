package com.webank.wedpr;

import java.security.SecureRandom;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.CryptoType;

public class KeyGeneratorMain {
    public static void main(String[] args) {
        // generateECDSAKey();
        generateSM2Key();
        generateSymmetricKey();
    }

    private static void generateECDSAKey() {
        CryptoSuite ecdsaCryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);
        CryptoKeyPair ecdsaCryptoKeyPair = ecdsaCryptoSuite.getCryptoKeyPair();
        String ecdsaHexPrivateKey = ecdsaCryptoKeyPair.getHexPrivateKey();
        String ecdsaHexPublicKey = ecdsaCryptoKeyPair.getHexPublicKey();
        System.out.println("ECDSA account is randomly generated as below:");
        System.out.println("ecdsa_private_key:" + ecdsaHexPrivateKey);
        System.out.println("ecdsa_public_key: 04" + ecdsaHexPublicKey);
    }

    private static void generateSM2Key() {
        CryptoSuite sm2CryptoSuite = new CryptoSuite(CryptoType.SM_TYPE);
        CryptoKeyPair sm2cryptoKeyPair = sm2CryptoSuite.getCryptoKeyPair();
        String sm2HexPrivateKey = sm2cryptoKeyPair.getHexPrivateKey();
        String sm2HexPublicKey = sm2cryptoKeyPair.getHexPublicKey();
        System.out.println();
        System.out.println("SM2 account is randomly generated as below:");
        System.out.println("sm2_private_key:" + sm2HexPrivateKey);
        System.out.println("sm2_public_key: 04" + sm2HexPublicKey);
    }

    private static void generateSymmetricKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes1 = new byte[16];
        byte[] bytes2 = new byte[32];
        random.nextBytes(bytes1);
        random.nextBytes(bytes2);
        String hexSymmetricKey1 = bytesToHex(bytes1);
        String hexSymmetricKey2 = bytesToHex(bytes2);
        System.out.println();
        System.out.println("symmetric_key(16 bytes):" + hexSymmetricKey1);
        System.out.println("symmetric_key(32 bytes):" + hexSymmetricKey2);
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
