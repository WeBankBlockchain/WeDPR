package com.webank.wedpr.components.task.plugin.pir.demo;

import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.components.crypto.CryptoToolkitFactory;
import com.webank.wedpr.components.crypto.SymmetricCrypto;
import java.nio.charset.StandardCharsets;

/** Created by caryliao on 2024/7/26 16:03 */
public class PirDemo {
    public static void main(String[] args) throws Exception {
        System.out.println(CryptoToolkitFactory.hash("caryliao"));
        System.out.println(CryptoToolkitFactory.hash("flyhuang"));
        String key = "test";
        SymmetricCrypto symmetricCrypto =
                CryptoToolkitFactory.buildAESSymmetricCrypto(
                        key, Constant.DEFAULT_IV.getBytes(StandardCharsets.UTF_8));
        String plainData = "testssdf";
        String cipher = symmetricCrypto.encrypt("plainData");
        String plain = symmetricCrypto.decrypt(cipher);
        System.out.println("### cipher: " + cipher);
        System.out.println("### plain: " + plain);
    }
}
