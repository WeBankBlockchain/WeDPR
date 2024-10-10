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

package com.webank.wedpr.components.task.plugin.pir.utils;

/** @author zachma */
public class Constant {
    public static String PIR_TEMP_TABLE_PREFIX = "wedpr_pir_";

    public static final String ID_FIELD_NAME = "id";
    public static final String ID_HASH_FIELD_NAME = "id_hash";

    public static String datasetId2tableId(String datasetId) {
        return PIR_TEMP_TABLE_PREFIX + datasetId.substring(2);
    }
}
