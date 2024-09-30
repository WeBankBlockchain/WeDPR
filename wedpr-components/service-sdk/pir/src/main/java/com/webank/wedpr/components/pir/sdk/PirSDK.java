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

package com.webank.wedpr.components.pir.sdk;

import com.webank.wedpr.components.pir.sdk.model.PirQueryParam;
import com.webank.wedpr.components.pir.sdk.model.PirResult;
import com.webank.wedpr.core.utils.WeDPRResponse;
import org.apache.commons.lang3.tuple.Pair;

public interface PirSDK {

    public abstract Pair<WeDPRResponse, PirResult> query(PirQueryParam queryParam) throws Exception;
}
