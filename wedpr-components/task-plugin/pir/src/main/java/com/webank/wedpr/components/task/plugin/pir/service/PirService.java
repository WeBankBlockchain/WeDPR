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

package com.webank.wedpr.components.task.plugin.pir.service;

import com.webank.wedpr.components.pir.sdk.core.ObfuscateData;
import com.webank.wedpr.components.pir.sdk.model.PirParamEnum;
import com.webank.wedpr.core.utils.WeDPRResponse;

public interface PirService {
    /**
     * query the data
     *
     * @param obfuscateData the query parm
     * @return the result
     */
    public abstract WeDPRResponse query(
            PirParamEnum.AlgorithmType algorithmType,
            String datasetID,
            ObfuscateData obfuscateData);

    /**
     * publish pir service
     *
     * @param datasetID the datasetID
     * @return the result
     */
    public abstract WeDPRResponse publish(String datasetID);
}
