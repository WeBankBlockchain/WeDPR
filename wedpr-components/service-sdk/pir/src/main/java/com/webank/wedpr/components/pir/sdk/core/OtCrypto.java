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

package com.webank.wedpr.components.pir.sdk.core;

import com.webank.wedpr.components.crypto.CryptoToolkitFactory;
import com.webank.wedpr.components.crypto.SymmetricCrypto;
import com.webank.wedpr.components.pir.sdk.model.PirParamEnum;
import com.webank.wedpr.components.pir.sdk.model.PirQueryParam;
import com.webank.wedpr.components.pir.sdk.model.PirResult;
import com.webank.wedpr.core.utils.Common;
import com.webank.wedpr.core.utils.Constant;
import com.webank.wedpr.core.utils.WeDPRException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OtCrypto {
    public static ObfuscateData generateOtParam(
            PirParamEnum.AlgorithmType algorithmType, PirQueryParam queryParam)
            throws WeDPRException {
        BigInteger blindingA = OtHelper.getRandomInt();
        BigInteger blindingB = OtHelper.getRandomInt();

        BigInteger x = OtHelper.powMod(blindingA);
        BigInteger y = OtHelper.powMod(blindingB);
        BigInteger blindingC = OtHelper.mulMod(blindingA, blindingB);
        if (algorithmType == PirParamEnum.AlgorithmType.idFilter) {
            return new ObfuscateData(
                    blindingB,
                    x,
                    y,
                    OtCrypto.generateOtParamForIDFilter(
                            blindingC, queryParam.getFilterLength(), queryParam.getSearchIdList()));
        }
        return new ObfuscateData(
                blindingB,
                x,
                y,
                OtCrypto.generateOtParamForIDObfuscation(
                        blindingC, queryParam.getObfuscationOrder(), queryParam.getSearchIdList()));
    }

    public static PirResult decryptAndGetResult(
            ObfuscateData obfuscateData, PirQueryParam queryParam, List<OtResult> otResultList) {
        return decryptResultAndObtainResult(
                obfuscateData.getB(), queryParam.getSearchIdList(), otResultList);
    }

    /* hash披露, 请求方选择id，生成随机数a、b */
    protected static List<ObfuscateData.ObfuscateDataItem> generateOtParamForIDFilter(
            BigInteger blindingC, Integer filterLength, List<String> searchIDList) {
        List<ObfuscateData.ObfuscateDataItem> obfuscateDataItems = new ArrayList<>();
        for (String searchId : searchIDList) {
            String filter =
                    searchId.length() < filterLength
                            ? searchId
                            : searchId.substring(0, filterLength);
            BigInteger z0 = calculateZ0(searchId, blindingC);
            ObfuscateData.ObfuscateDataItem pirDataBody = new ObfuscateData.ObfuscateDataItem();
            pirDataBody.setFilter(filter);
            pirDataBody.setZ0(z0);
            obfuscateDataItems.add(pirDataBody);
        }
        return obfuscateDataItems;
    }

    /* hash筛选, 请求方选择顺序\delta\in \{0,1,..,m-1\}，生成随机数a、b */
    protected static List<ObfuscateData.ObfuscateDataItem> generateOtParamForIDObfuscation(
            BigInteger blindingC, Integer obfuscationOrder, List<String> searchIDList)
            throws WeDPRException {
        List<ObfuscateData.ObfuscateDataItem> obfuscateDataItems = new ArrayList<>();
        Random rand = new Random();
        for (String searchId : searchIDList) {
            int idIndex = rand.nextInt(obfuscationOrder + 1);
            BigInteger z0 = calculateIndexZ0(idIndex, blindingC);
            List<String> idHashVecList = OtHelper.getIdHashVec(obfuscationOrder, idIndex, searchId);

            ObfuscateData.ObfuscateDataItem obfuscateDataItem =
                    new ObfuscateData.ObfuscateDataItem();
            obfuscateDataItem.setZ0(z0);
            obfuscateDataItem.setIdIndex(idIndex);
            obfuscateDataItem.setIdHashList(idHashVecList);
            obfuscateDataItems.add(obfuscateDataItem);
        }
        return obfuscateDataItems;
    }

    private static BigInteger calculateZ0(String searchId, BigInteger blindingC) {
        byte[] idBytes = searchId.getBytes(StandardCharsets.UTF_8);
        BigInteger idNumber = Common.bytesToBigInteger(idBytes);
        return OtHelper.powMod(blindingC.subtract(idNumber));
    }

    private static BigInteger calculateIndexZ0(Integer idIndex, BigInteger blindingC) {
        return OtHelper.powMod(blindingC.subtract(BigInteger.valueOf(idIndex)));
    }

    private static void decryptServerResultList(
            PirResult.PirResultItem pirResultItem, OtResult otResultList, BigInteger b) {
        for (OtResult.OtResultItem result : otResultList.getOtResultItems()) {
            BigInteger e = result.getE();
            BigInteger w = result.getW();
            String cipherStr = result.getC();
            BigInteger w1 = OtHelper.OTPow(w, b);
            try {
                String key =
                        new String(Common.bigIntegerToBytes(w1.xor(e)), StandardCharsets.UTF_8);
                SymmetricCrypto symmetricCrypto =
                        CryptoToolkitFactory.buildAESSymmetricCrypto(
                                key, Constant.DEFAULT_IV.getBytes(StandardCharsets.UTF_8));
                String decryptedText = symmetricCrypto.decrypt(cipherStr);
                pirResultItem.setValue(decryptedText);
            } catch (Exception ignored) {

            }
        }
    }

    protected static PirResult decryptResultAndObtainResult(
            BigInteger blindingB, List<String> seachIDList, List<OtResult> otResultList) {
        List<PirResult.PirResultItem> pirResultItemList = new ArrayList<>();
        for (int i = 0; i < seachIDList.size(); i++) {
            PirResult.PirResultItem pirResultItem = new PirResult.PirResultItem();
            pirResultItem.setSearchId(seachIDList.get(i));
            decryptServerResultList(pirResultItem, otResultList.get(i), blindingB);
            pirResultItemList.add(pirResultItem);
        }
        return new PirResult(pirResultItemList);
    }
}
