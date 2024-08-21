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

package com.webank.wedpr.components.meta.setting.template.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.webank.wedpr.components.meta.setting.template.dao.SettingTemplateDO;
import com.webank.wedpr.core.config.WeDPRCommonConfig;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TemplateSettingRequest {
    private List<SettingTemplateDO> templateList;

    public List<SettingTemplateDO> getTemplateList() {
        return templateList;
    }

    public void setTemplateList(List<SettingTemplateDO> templateList) {
        this.templateList = templateList;
    }

    public void checkCreate(Boolean admin) {
        for (SettingTemplateDO setting : templateList) {
            setting.checkInsert(admin);
        }
    }

    public void checkUpdate(String owner, Boolean admin) {
        for (SettingTemplateDO setting : templateList) {
            setting.checkUpdate(owner, admin);
        }
    }

    public void setOwnerInfo(String owner) {
        for (SettingTemplateDO template : templateList) {
            template.setOwner(owner);
            template.setAgency(WeDPRCommonConfig.getAgency());
        }
    }
}
