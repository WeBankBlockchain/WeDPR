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

import com.github.yitter.contract.IdGeneratorOptions;
import com.github.yitter.idgen.YitIdHelper;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.uuid.generator.UuidGenerator;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YitterUuidGenerator implements UuidGenerator {
    private static final Logger logger = LoggerFactory.getLogger(YitterUuidGenerator.class);

    @SneakyThrows(WeDPRException.class)
    public YitterUuidGenerator(YitterUuidConfig yitterUuidConfig) {
        yitterUuidConfig.check();
        try {
            Short workerID = Short.valueOf(yitterUuidConfig.getWorkerID());
            IdGeneratorOptions options = new IdGeneratorOptions(workerID);
            options.SeqBitLength = yitterUuidConfig.getSeqBitLength().byteValue();
            options.WorkerIdBitLength = yitterUuidConfig.getWorkerIDBitLength().byteValue();
            YitIdHelper.setIdGenerator(options);
        } catch (Exception e) {
            logger.error(
                    "create YitterUuidGenerator error, machineID: {}, e: ",
                    yitterUuidConfig.getWorkerID(),
                    e);
            throw new WeDPRException(
                    "Create YitterUuidGenerator error, machineID: "
                            + yitterUuidConfig.getWorkerID()
                            + ", error: "
                            + e.getMessage(),
                    e);
        }
    }

    @Override
    public String generateID() {
        return String.valueOf(YitIdHelper.nextId());
    }
}
