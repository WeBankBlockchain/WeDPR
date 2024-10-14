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
import com.webank.wedpr.common.config.WeDPRCommonConfig;
import com.webank.wedpr.common.utils.Common;
import com.webank.wedpr.common.utils.TimeRange;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.uuid.generator.WeDPRUuidGenerator;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAgency() {
        return agency;
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public String getSetting() {
        return setting;
    }

    public void setSetting(String setting) {
        this.setting = setting;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<String> getQueriedOwners() {
        return queriedOwners;
    }

    public void setQueriedOwners(List<String> queriedOwners) {
        this.queriedOwners = queriedOwners;
    }

    @SneakyThrows(WeDPRException.class)
    public void checkInsert(Boolean admin) {
        Common.requireNonEmpty("id", this.id);
        Common.requireNonEmpty("name", this.name);
        Common.requireNonEmpty("type", this.type);
        Common.requireNonEmpty("setting", this.setting);
        if (admin) {
            return;
        }
        if (StringUtils.isNotBlank(this.owner) && this.owner.equals(DEFAULT_TEMPLATE_OWNER)) {
            throw new WeDPRException(
                    "Only the admins have permissions to insert default settings!");
        }
    }

    public void checkUpdate(String owner, Boolean admin) {
        Common.requireNonEmpty("id", this.id);
        if (admin) {
            return;
        }
        // the non-admin can only update themselves record
        setOwner(owner);
        setAgency(WeDPRCommonConfig.getAgency());
    }

    @Override
    public String toString() {
        return "SettingTemplateDO{"
                + "id='"
                + id
                + '\''
                + ", name='"
                + name
                + '\''
                + ", type='"
                + type
                + '\''
                + ", agency='"
                + agency
                + '\''
                + ", owner='"
                + owner
                + '\''
                + ", setting='"
                + setting
                + '\''
                + '}';
    }
}
