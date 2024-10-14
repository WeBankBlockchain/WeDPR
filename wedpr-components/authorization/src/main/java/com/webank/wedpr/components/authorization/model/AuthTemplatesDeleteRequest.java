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
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;

public class AuthTemplatesDeleteRequest {
    private String createUser;
    private List<String> templates = new ArrayList<>();

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public List<String> getTemplates() {
        return templates;
    }

    public void setTemplates(List<String> templates) {
        if (templates == null) {
            return;
        }
        this.templates = templates;
    }

    @Override
    public String toString() {
        return "AuthTemplatesDeleteRequest{"
                + "createUser='"
                + createUser
                + '\''
                + ", templates="
                + templates
                + '}';
    }

    @SneakyThrows(Exception.class)
    public String serialize() {
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
    }

    @SneakyThrows(Exception.class)
    public static AuthTemplatesDeleteRequest deserialize(String data) {
        return ObjectMapperFactory.getObjectMapper()
                .readValue(data, AuthTemplatesDeleteRequest.class);
    }
}
