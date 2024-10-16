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

package com.webank.wedpr.components.meta.setting.template.service;

import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.meta.setting.template.model.TemplateSettingQueryRequest;
import com.webank.wedpr.components.meta.setting.template.model.TemplateSettingRequest;
import java.util.List;

public interface TemplateSettingService {
    public abstract WeDPRResponse batchInsertTemplateSettings(
            Boolean admin, String user, TemplateSettingRequest settings) throws Exception;

    public abstract WeDPRResponse batchUpdateTemplateSettings(
            Boolean admin, String user, TemplateSettingRequest settings) throws Exception;

    public abstract WeDPRResponse deleteTemplateSettings(
            Boolean admin, String user, List<String> templateIDList);

    public abstract WeDPRResponse querySettings(
            Boolean admin, String user, TemplateSettingQueryRequest request);
}
