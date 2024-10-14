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
package com.webank.wedpr.components.spi.plugin;

import com.webank.wedpr.common.utils.WeDPRException;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SPILoader<T extends SPIObject> {
    private static final Logger logger = LoggerFactory.getLogger(SPILoader.class);
    private final Map<String, T> spiObjectMap = new HashMap<>();

    @SneakyThrows(Exception.class)
    public SPILoader(Class<T> spiClass) {
        for (T spiObject : ServiceLoader.load(spiClass)) {
            // load new spi object
            if (!spiObjectMap.containsKey(spiObject.getSpiInfo().getName())) {
                spiObjectMap.put(spiObject.getSpiInfo().getName(), spiObject);
                continue;
            }
            // spi conflict, load the higher priority object
            T existedSPIObject = spiObjectMap.get(spiObject.getSpiInfo().getName());
            if (existedSPIObject.equals(spiObject)) {
                String errorMsg =
                        String.format(
                                "load spi failed for conflict, there are two spi objects with same name '%s' and priority '%s'",
                                spiObject.getSpiInfo().getName(),
                                spiObject.getSpiInfo().getPriority());
                logger.error(errorMsg);
                throw new WeDPRException(errorMsg);
            }
            if (existedSPIObject.compareTo(spiObject) > 0) {
                logger.info(
                        "load new spi object with higher priority, name: {}, oldPriority: {}, newPriority: {}",
                        spiObject.getSpiInfo().getName(),
                        existedSPIObject.getSpiInfo().getPriority(),
                        spiObject.getSpiInfo().getPriority());
                spiObjectMap.put(spiObject.getSpiInfo().getName(), spiObject);
                continue;
            }
            logger.info(
                    "Ignore spi object with lower priority, name: {}, priority: {}, currentPriority: {}",
                    spiObject.getSpiInfo().getName(),
                    spiObject.getSpiInfo().getPriority(),
                    existedSPIObject.getSpiInfo().getPriority());
        }
    }

    public Map<String, T> getSpiObjectMap() {
        return spiObjectMap;
    }

    public T getSPIObjectByName(String spiName) {
        if (!spiObjectMap.containsKey(spiName)) {
            return null;
        }
        return spiObjectMap.get(spiName);
    }
}
