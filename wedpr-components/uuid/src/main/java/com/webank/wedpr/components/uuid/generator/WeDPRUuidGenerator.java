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

package com.webank.wedpr.components.uuid.generator;

import com.webank.wedpr.common.config.WeDPRConfig;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.uuid.generator.yitter.YitterUuidConfig;
import com.webank.wedpr.components.uuid.generator.yitter.YitterUuidGenerator;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeDPRUuidGenerator {
    private static final Logger logger = LoggerFactory.getLogger(WeDPRUuidGenerator.class);
    private static UuidGenerator uuidGenerator;

    static {
        createUUIDGenerator();
    }

    @SneakyThrows(WeDPRException.class)
    private static void createUUIDGenerator() {
        String uuidGeneratorType = WeDPRConfig.apply("wedpr.uuid.generator", "yitter");
        logger.info("Create UuidGenerator, type: {}", uuidGeneratorType);
        if (uuidGeneratorType.compareToIgnoreCase("yitter") == 0) {
            uuidGenerator = new YitterUuidGenerator(new YitterUuidConfig());
            logger.info("Create UuidGenerator success, type: {}", uuidGeneratorType);
        } else {
            throw new WeDPRException("Unsupported uuid-generator " + uuidGeneratorType);
        }
    }

    public static String generateID() {
        return uuidGenerator.generateID();
    }
}
