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

import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.db.mapper.service.publish.model.PirServiceSetting;
import com.webank.wedpr.components.pir.sdk.model.PirQueryRequest;

public interface PirService {

    /**
     * query the data
     *
     * @param pirQueryRequest the pir query request
     * @return the query result
     */
    public abstract WeDPRResponse query(PirQueryRequest pirQueryRequest) throws Exception;

    /**
     * publish pir service
     *
     * @param serviceID the serviceID
     * @return the result
     */
    public abstract WeDPRResponse publish(String serviceID, PirServiceSetting pirServiceSetting);
}
