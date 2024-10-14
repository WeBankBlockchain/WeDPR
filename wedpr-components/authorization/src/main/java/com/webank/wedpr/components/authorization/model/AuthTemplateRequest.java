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

package com.webank.wedpr.components.authorization.model;

import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.components.authorization.dao.AuthorizationTemplateDO;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

public class AuthTemplateRequest {
    private List<AuthorizationTemplateDO> templateList;

    public List<AuthorizationTemplateDO> getTemplateList() {
        return templateList;
    }

    public void setTemplateList(List<AuthorizationTemplateDO> templateList) {
        this.templateList = templateList;
    }

    @SneakyThrows(Exception.class)
    public String serialize() {
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
    }

    public void checkCreate(String user) {
        for (AuthorizationTemplateDO templateDO : templateList) {
            templateDO.checkCreate(user);
        }
    }

    public void checkUpdate(String user) {
        for (AuthorizationTemplateDO templateDO : templateList) {
            templateDO.checkUpdate(user);
        }
    }

    @Override
    public String toString() {
        return "AuthTemplateRequest{" + "templateList=" + templateList + '}';
    }

    @SneakyThrows(Exception.class)
    public static AuthTemplateRequest deserialize(String data) {
        if (StringUtils.isBlank(data)) {
            return null;
        }
        return ObjectMapperFactory.getObjectMapper().readValue(data, AuthTemplateRequest.class);
    }

    public List<String> getTemplateIDList() {
        List<String> templateIDList = new ArrayList<>();
        for (AuthorizationTemplateDO templateDO : templateList) {
            templateIDList.add(templateDO.getTemplateID());
        }
        return templateIDList;
    }
}
