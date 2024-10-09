package com.webank.wedpr.components.crypto;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.util.encoders.Hex;

public class SM2Helper {

    private static final X9ECParameters x9ECParameters = GMNamedCurves.getByName("sm2p256v1");

    private static final ECParameterSpec ecDomainParameters =
            new ECParameterSpec(
                    x9ECParameters.getCurve(), x9ECParameters.getG(), x9ECParameters.getN());

    public static String encrypt(String publicKeyHex, String data)
            throws InvalidCipherTextException {
        return encrypt(getECPublicKeyByPublicKeyHex(publicKeyHex), data, SM2Engine.Mode.C1C3C2);
    }

    private static String encrypt(BCECPublicKey publicKey, String data, SM2Engine.Mode modeType)
            throws InvalidCipherTextException {
        // 加密模式
        ECParameterSpec ecParameterSpec = publicKey.getParameters();
        ECDomainParameters ecDomainParameters =
                new ECDomainParameters(
                        ecParameterSpec.getCurve(), ecParameterSpec.getG(), ecParameterSpec.getN());
        ECPublicKeyParameters ecPublicKeyParameters =
                new ECPublicKeyParameters(publicKey.getQ(), ecDomainParameters);
        SM2Engine sm2Engine = new SM2Engine(modeType);
        sm2Engine.init(true, new ParametersWithRandom(ecPublicKeyParameters, new SecureRandom()));
        byte[] in = data.getBytes(StandardCharsets.UTF_8);
        byte[] arrayOfBytes = sm2Engine.processBlock(in, 0, in.length);
        return Hex.toHexString(arrayOfBytes);
    }

    public static String decrypt(String privateKeyHex, String cipherData)
            throws InvalidCipherTextException {
        return decrypt(
                getBCECPrivateKeyByPrivateKeyHex(privateKeyHex), cipherData, SM2Engine.Mode.C1C3C2);
    }

    private static String decrypt(
            BCECPrivateKey privateKey, String cipherData, SM2Engine.Mode modeType)
            throws InvalidCipherTextException {
        // 解密模式
        byte[] cipherDataByte = Hex.decode(cipherData);
        ECParameterSpec ecParameterSpec = privateKey.getParameters();
        ECDomainParameters ecDomainParameters =
                new ECDomainParameters(
                        ecParameterSpec.getCurve(), ecParameterSpec.getG(), ecParameterSpec.getN());
        ECPrivateKeyParameters ecPrivateKeyParameters =
                new ECPrivateKeyParameters(privateKey.getD(), ecDomainParameters);

        SM2Engine sm2Engine = new SM2Engine(modeType);
        sm2Engine.init(false, ecPrivateKeyParameters);
        byte[] arrayOfBytes = sm2Engine.processBlock(cipherDataByte, 0, cipherDataByte.length);
        return new String(arrayOfBytes, StandardCharsets.UTF_8);
    }

    private static BCECPublicKey getECPublicKeyByPublicKeyHex(String pubKeyHex) {
        if (pubKeyHex.length() > 128) {
            pubKeyHex = pubKeyHex.substring(pubKeyHex.length() - 128);
        }
        String stringX = pubKeyHex.substring(0, 64);
        String stringY = pubKeyHex.substring(stringX.length());
        BigInteger x = new BigInteger(stringX, 16);
        BigInteger y = new BigInteger(stringY, 16);
        ECPublicKeySpec ecPublicKeySpec =
                new ECPublicKeySpec(
                        x9ECParameters.getCurve().createPoint(x, y), ecDomainParameters);
        return new BCECPublicKey("EC", ecPublicKeySpec, BouncyCastleProvider.CONFIGURATION);
    }

    private static BCECPrivateKey getBCECPrivateKeyByPrivateKeyHex(String privateKeyHex) {
        BigInteger d = new BigInteger(privateKeyHex, 16);
        ECPrivateKeySpec ecPrivateKeySpec = new ECPrivateKeySpec(d, ecDomainParameters);
        return new BCECPrivateKey("EC", ecPrivateKeySpec, BouncyCastleProvider.CONFIGURATION);
    }
}
