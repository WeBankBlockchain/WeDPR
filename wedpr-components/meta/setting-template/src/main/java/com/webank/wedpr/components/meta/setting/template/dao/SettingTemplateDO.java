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

package com.webank.wedpr.components.meta.setting.template.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.webank.wedpr.common.config.WeDPRCommonConfig;
import com.webank.wedpr.common.utils.Common;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.common.utils.TimeRange;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.uuid.generator.WeDPRUuidGenerator;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

@Data
@ToString
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SettingTemplateDO extends TimeRange {
    public static final String DEFAULT_TEMPLATE_OWNER = "*";
    private String id = WeDPRUuidGenerator.generateID();
    private String name;
    private String type;
    private String agency;
    private String owner;
    private String setting;
    private String createTime;
    private String lastUpdateTime;
    @JsonIgnore private transient List<String> queriedOwners;

    @SneakyThrows(WeDPRException.class)
    public void checkInsert(Boolean admin) {
        Common.requireNonEmpty("id", this.id);
        Common.requireNonEmpty("name", this.name);
        Common.requireNonEmpty("type", this.type);
        Common.requireNonEmpty("setting", this.setting);
        if (admin) {
            return;
        }
        if (getName().startsWith(Constant.RESERVE_SETTING_PREFIX)) {
            throw new WeDPRException(
                    "The non-admin user can't update or create the settings start with "
                            + Constant.RESERVE_SETTING_PREFIX);
        }
    }

    public void checkUpdate(String owner, Boolean admin) throws Exception {
        Common.requireNonEmpty("id", this.id);
        if (admin) {
            return;
        }
        if (getName().startsWith(Constant.RESERVE_SETTING_PREFIX)) {
            throw new WeDPRException(
                    "The non-admin user can't update or create the settings start with "
                            + Constant.RESERVE_SETTING_PREFIX);
        }
        // the non-admin can only update themselves record
        setOwner(owner);
        setAgency(WeDPRCommonConfig.getAgency());
    }

    public static SettingTemplateDO deserialize(String data) throws Exception {
        if (StringUtils.isBlank(data)) {
            return null;
        }
        return ObjectMapperFactory.getObjectMapper().readValue(data, SettingTemplateDO.class);
    }
}
