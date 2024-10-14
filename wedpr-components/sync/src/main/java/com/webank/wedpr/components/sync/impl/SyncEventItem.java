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
package com.webank.wedpr.components.sync.impl;

import com.webank.wedpr.common.utils.WeDPRException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyncEventItem {
    private static Logger logger = LoggerFactory.getLogger(SyncEventItem.class);
    private String recordAddress;
    private BigInteger recordIndex;
    private BigInteger contractVersion = BigInteger.ONE;

    @SneakyThrows(Exception.class)
    public SyncEventItem(List<Object> eventData) {
        if (eventData.size() < 2) {
            throw new WeDPRException(
                    "Invalid eventData: "
                            + Arrays.toString(eventData.toArray())
                            + ", must contain recordAddress and recordIndex");
        }
        recordAddress = (String) eventData.get(0);
        recordIndex = (BigInteger) eventData.get(1);
        logger.debug("Decode SyncEventItem, address: {}, index: {}", recordAddress, recordIndex);
        if (eventData.size() >= 3) {
            contractVersion = (BigInteger) eventData.get(2);
        }
    }

    public String getRecordAddress() {
        return recordAddress;
    }

    public void setRecordAddress(String recordAddress) {
        this.recordAddress = recordAddress;
    }

    public BigInteger getRecordIndex() {
        return recordIndex;
    }

    public void setRecordIndex(BigInteger recordIndex) {
        this.recordIndex = recordIndex;
    }

    public BigInteger getContractVersion() {
        return contractVersion;
    }

    public void setContractVersion(BigInteger contractVersion) {
        this.contractVersion = contractVersion;
    }

    @Override
    public String toString() {
        return "SyncEventItem{"
                + "recordAddress="
                + recordAddress
                + ", recordIndex="
                + recordIndex
                + ", contractVersion="
                + contractVersion
                + '}';
    }
}
