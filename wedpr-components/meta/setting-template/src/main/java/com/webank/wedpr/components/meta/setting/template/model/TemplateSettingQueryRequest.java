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

import com.webank.wedpr.common.utils.PageRequest;
import com.webank.wedpr.components.meta.setting.template.dao.SettingTemplateDO;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;

public class TemplateSettingQueryRequest extends PageRequest {
    private Boolean onlyMeta = true;
    private SettingTemplateDO condition;

    public Boolean getOnlyMeta() {
        return onlyMeta;
    }

    public void setOnlyMeta(Boolean onlyMeta) {
        this.onlyMeta = onlyMeta;
    }

    public SettingTemplateDO getCondition() {
        return condition;
    }

    public void setCondition(SettingTemplateDO condition) {
        this.condition = condition;
    }

    public void setOwnerCondition(Boolean admin, String user) {
        // the admin can query any records
        if (admin) {
            return;
        }
        // the non-admin can query the default-setting
        if (StringUtils.isNotBlank(condition.getOwner())
                && condition.getOwner().equals(SettingTemplateDO.DEFAULT_TEMPLATE_OWNER)) {
            return;
        }
        // the non-admin can query the self-setting
        condition.setQueriedOwners(
                new ArrayList<>(Arrays.asList(user, SettingTemplateDO.DEFAULT_TEMPLATE_OWNER)));
        return;
    }

    @Override
    public String toString() {
        return "TemplateSettingQueryRequest{"
                + "onlyMeta="
                + onlyMeta
                + ", condition="
                + condition
                + '}';
    }
}
