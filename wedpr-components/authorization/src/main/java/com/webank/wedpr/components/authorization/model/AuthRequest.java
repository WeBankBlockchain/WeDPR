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
import com.webank.wedpr.components.authorization.dao.AuthorizationDO;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

public class AuthRequest {
    // Note: replace the existed followers when update the auth or not, only set to true when update
    // the content
    private Boolean resetFollowers = false;
    private List<AuthorizationDO> authList = new ArrayList<>();

    public AuthRequest() {}

    public AuthRequest(List<String> authList, String status) {
        for (String id : authList) {
            AuthorizationDO authorizationDO = new AuthorizationDO(id, status);
            this.authList.add(authorizationDO);
        }
    }

    public AuthRequest(AuthorizationDO authorizationDO) {
        this.authList.add(authorizationDO);
    }

    public AuthRequest(AuthorizationDO authorizationDO, boolean resetFollowers) {
        this.authList.add(authorizationDO);
        this.resetFollowers = resetFollowers;
    }

    public Boolean isResetFollowers() {
        return resetFollowers;
    }

    public void setResetFollowers(Boolean resetFollowers) {
        if (resetFollowers == null) {
            return;
        }
        this.resetFollowers = resetFollowers;
    }

    public List<AuthorizationDO> getAuthList() {
        return authList;
    }

    public void setAuthList(List<AuthorizationDO> authList) {
        this.authList = authList;
    }

    @SneakyThrows(Exception.class)
    public String serialize() {
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
    }

    @SneakyThrows(Exception.class)
    public static AuthRequest deserialize(String authRequest) {
        if (StringUtils.isBlank(authRequest)) {
            return null;
        }
        return ObjectMapperFactory.getObjectMapper().readValue(authRequest, AuthRequest.class);
    }

    public List<String> getAuthIDList() {
        List<String> authIDList = new ArrayList<>();
        for (AuthorizationDO authDO : authList) {
            authIDList.add(authDO.getId());
        }
        return authIDList;
    }

    @Override
    public String toString() {
        return "AuthRequest{" + "resetFollowers=" + resetFollowers + ", authList=" + authList + '}';
    }
}
