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

import com.webank.wedpr.common.utils.PageRequest;
import com.webank.wedpr.components.authorization.dao.AuthorizationDO;
import com.webank.wedpr.components.meta.resource.follower.dao.FollowerDO;

public class AuthFollowerRequest extends PageRequest {
    private FollowerDO authFollowerDO = new FollowerDO();
    private AuthorizationDO condition = new AuthorizationDO(true);

    public FollowerDO getAuthFollowerDO() {
        return authFollowerDO;
    }

    public void setAuthFollowerDO(FollowerDO authFollowerDO) {
        if (authFollowerDO == null) {
            return;
        }
        this.authFollowerDO = authFollowerDO;
    }

    public AuthorizationDO getCondition() {
        return condition;
    }

    public void setCondition(AuthorizationDO condition) {
        if (condition == null) {
            return;
        }
        this.condition = condition;
    }
}
