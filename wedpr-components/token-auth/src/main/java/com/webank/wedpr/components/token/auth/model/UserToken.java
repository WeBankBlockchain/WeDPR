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

package com.webank.wedpr.components.token.auth.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.webank.wedpr.core.protocol.UserRoleEnum;
import com.webank.wedpr.core.utils.ObjectMapperFactory;
import java.util.List;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class UserToken {
    private String username;
    private List<GroupInfo> groupInfos;
    private String roleName;
    private List<String> permissions;
    // used for service access auth
    private String accessKeyID;

    public UserToken() {}

    public UserToken(String userName, String roleName, List<GroupInfo> groupInfos) {
        this.username = userName;
        this.roleName = roleName;
        this.groupInfos = groupInfos;
    }

    public Boolean isAdmin() {
        if (StringUtils.isBlank(roleName)) {
            return Boolean.FALSE;
        }
        if (roleName.contains(UserRoleEnum.ADMIN_ROLE.getRoleName())) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    public String serialize() throws JsonProcessingException {
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
    }

    public static UserToken deserialize(String data) throws JsonProcessingException {
        if (StringUtils.isBlank(data)) {
            return null;
        }
        return ObjectMapperFactory.getObjectMapper().readValue(data, UserToken.class);
    }
}
