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

package com.webank.wedpr.components.task.plugin.pir.core.impl;

import com.webank.wedpr.common.utils.Common;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.components.crypto.CryptoToolkitFactory;
import com.webank.wedpr.components.crypto.SymmetricCrypto;
import com.webank.wedpr.components.pir.sdk.core.ObfuscateData;
import com.webank.wedpr.components.pir.sdk.core.OtHelper;
import com.webank.wedpr.components.pir.sdk.core.OtResult;
import com.webank.wedpr.components.task.plugin.pir.core.Obfuscator;
import com.webank.wedpr.components.task.plugin.pir.model.ObfuscationParam;
import com.webank.wedpr.components.task.plugin.pir.model.PirDataItem;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObfuscatorImpl implements Obfuscator {
    private final Logger logger = LoggerFactory.getLogger(ObfuscatorImpl.class);

    @Override
    public List<OtResult.OtResultItem> obfuscate(
            ObfuscationParam param,
            List<PirDataItem> pirDataItems,
            ObfuscateData.ObfuscateDataItem obfuscateDataItem)
            throws Exception {
        List<OtResult.OtResultItem> resultItems = new ArrayList<>();
        for (PirDataItem pirDataItem : pirDataItems) {
            param.setPirDataItem(pirDataItem);
            resultItems.add(obfuscate(param, obfuscateDataItem));
        }
        return resultItems;
    }

    @Override
    public OtResult.OtResultItem obfuscate(
            ObfuscationParam param, ObfuscateData.ObfuscateDataItem obfuscateDataItem)
            throws Exception {
        if (obfuscateDataItem == null) {
            return null;
        }
        String message = param.getPirDataItem().getPirValue();

        BigInteger blindingR = OtHelper.getRandomInt();
        BigInteger blindingS = OtHelper.getRandomInt();
        BigInteger w =
                OtHelper.OTMul(OtHelper.OTPow(param.getX(), blindingS), OtHelper.powMod(blindingR));
        BigInteger z1 =
                OtHelper.OTMul(
                        obfuscateDataItem.getZ0(), OtHelper.powMod(param.getObfuscationValue()));
        BigInteger key =
                OtHelper.OTMul(
                        OtHelper.OTPow(z1, blindingS), OtHelper.OTPow(param.getY(), blindingR));

        String aesKey = CryptoToolkitFactory.generateRandomKey();
        BigInteger aesNum = Common.bytesToBigInteger(aesKey.getBytes(StandardCharsets.UTF_8));
        BigInteger messageCipherNum = key.xor(aesNum);

        SymmetricCrypto symmetricCrypto =
                CryptoToolkitFactory.buildAESSymmetricCrypto(
                        aesKey, Constant.DEFAULT_IV.getBytes(StandardCharsets.UTF_8));
        OtResult.OtResultItem otResultItem =
                new OtResult.OtResultItem(messageCipherNum, w, symmetricCrypto.encrypt(message));
        // return the pir key to check matching
        otResultItem.setPirKey(param.getPirDataItem().getPirKey());
        return otResultItem;
    }
}
