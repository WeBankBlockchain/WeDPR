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

package com.webank.wedpr.components.uuid.generator.yitter;

import com.webank.wedpr.common.config.WeDPRConfig;
import com.webank.wedpr.common.utils.WeDPRException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YitterUuidConfig {
    private static final Logger logger = LoggerFactory.getLogger(YitterUuidConfig.class);
    private Short workerIDBitLength;
    private String workerID;
    private Short seqBitLength;

    public YitterUuidConfig() {
        logger.info("create YitterUuidConfig");
        this.workerIDBitLength =
                WeDPRConfig.apply("wedpr.uuid.generator.worker.id.bit.len", Short.valueOf("6"));
        this.seqBitLength =
                WeDPRConfig.apply("wedpr.uuid.generator.seq.bit.length", Short.valueOf("10"));
        this.workerID = (String) WeDPRConfig.apply("wedpr.uuid.generator.worker.id", "10");
        logger.info("create YitterUuidConfig success, detail: {}", toString());
    }

    public Short getWorkerIDBitLength() {
        return workerIDBitLength;
    }

    public void setWorkerIDBitLength(Short workerIDBitLength) {
        this.workerIDBitLength = workerIDBitLength;
    }

    public String getWorkerID() {
        return workerID;
    }

    public void setWorkerID(String workerID) {
        this.workerID = workerID;
    }

    public Short getSeqBitLength() {
        return seqBitLength;
    }

    public void setSeqBitLength(Short seqBitLength) {
        this.seqBitLength = seqBitLength;
    }

    public void check() throws WeDPRException {
        if (StringUtils.isBlank(workerID)) {
            throw new WeDPRException("create YitterUuidGenerator for empty workerID!");
        }
    }

    @Override
    public String toString() {
        return "YitterUuidConfig{"
                + "workerIDBitLength="
                + workerIDBitLength
                + ", workerID='"
                + workerID
                + '\''
                + ", seqBitLength="
                + seqBitLength
                + '}';
    }
}
