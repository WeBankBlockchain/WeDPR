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
package com.webank.wedpr.common.protocol;

import org.apache.commons.lang3.StringUtils;

// the storage type
public enum StorageType {
    HDFS("HDFS"), // the hdfs
    SQL("SQL"),
    HIVE("HIVE"), // hive
    LOCAL("LOCAL"), // local file
    UNKNOWN("UNKNOWN");

    private final String name;

    StorageType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static StorageType deserialize(String type) {
        if (StringUtils.isBlank(type)) {
            return null;
        }
        for (StorageType storageType : StorageType.values()) {
            if (storageType.name.compareToIgnoreCase(type) == 0) {
                return storageType;
            }
        }
        return null;
    }
}
