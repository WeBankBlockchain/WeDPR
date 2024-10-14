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

package com.webank.wedpr.components.authorization.dao;

import com.webank.wedpr.common.utils.Common;
import com.webank.wedpr.common.utils.TimeRange;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.uuid.generator.WeDPRUuidGenerator;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

public class AuthorizationTemplateDO extends TimeRange {
    private String templateID = WeDPRUuidGenerator.generateID();
    private String templateName;
    private String templateDesc;
    private String templateSetting;
    private String createUser;
    private String createTime;
    private String lastUpdateTime;

    public AuthorizationTemplateDO() {}

    public AuthorizationTemplateDO(boolean resetID) {
        if (resetID) {
            this.templateID = "";
        }
    }

    public AuthorizationTemplateDO(String templateName) {
        setTemplateName(templateName);
    }

    public String getTemplateID() {
        return templateID;
    }

    public void setTemplateID(String templateID) {
        this.templateID = templateID;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateDesc() {
        return templateDesc;
    }

    public void setTemplateDesc(String templateDesc) {
        this.templateDesc = templateDesc;
    }

    public String getTemplateSetting() {
        return templateSetting;
    }

    public void setTemplateSetting(String templateSetting) {
        this.templateSetting = templateSetting;
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

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public void checkCreate(String user) {
        setCreateUser(user);
        Common.requireNonEmpty("templateName", getTemplateName());
        Common.requireNonEmpty("templateSetting", getTemplateSetting());
        // TODO: check the templateSetting
    }

    @SneakyThrows(WeDPRException.class)
    public void checkUpdate(String user) {
        setCreateUser(user);
        Common.requireNonEmpty("templateID", getTemplateID());
        if (StringUtils.isBlank(getTemplateDesc()) && StringUtils.isBlank(getTemplateSetting())) {
            throw new WeDPRException(
                    "Invalid updateAuthTemplate request for nothing to change! templateID: "
                            + getTemplateID()
                            + ", user: "
                            + user);
        }
        // TODO: check the templateSetting
    }

    @Override
    public String toString() {
        return "AuthorizationTemplateDO{"
                + "templateID='"
                + templateID
                + '\''
                + ", templateName='"
                + templateName
                + '\''
                + ", createUser='"
                + createUser
                + '\''
                + ", templateDesc='"
                + templateDesc
                + '\''
                + ", templateSetting='"
                + templateSetting
                + '\''
                + ", createTime='"
                + createTime
                + '\''
                + ", lastUpdateTime='"
                + lastUpdateTime
                + '\''
                + '}';
    }
}
