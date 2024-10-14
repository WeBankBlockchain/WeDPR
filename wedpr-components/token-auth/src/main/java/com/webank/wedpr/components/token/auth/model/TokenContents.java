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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRException;
import java.util.HashMap;
import java.util.Map;

public class TokenContents {
    private Map<String, String> tokenContents = new HashMap<>();

    public TokenContents() {}

    public void addTokenContents(String key, String value) {
        tokenContents.put(key, value);
    }

    public Map<String, String> getTokenContents() {
        return tokenContents;
    }

    public void setTokenContents(Map<String, String> tokenContents) {
        this.tokenContents = tokenContents;
    }

    public String getTokenContentByKey(String key) {
        if (!tokenContents.containsKey(key)) {
            return null;
        }
        return tokenContents.get(key);
    }

    public UserToken getUserToken() throws Exception {
        String content = getTokenContentByKey(Constant.USER_TOKEN_CLAIM);
        UserToken userToken = UserToken.deserialize(content);
        if (userToken == null) {
            throw new WeDPRException("the userToken not been set!");
        }
        return userToken;
    }

    public void setUserToken(UserToken userToken) throws JsonProcessingException {
        if (userToken == null) {
            return;
        }
        this.tokenContents.put(Constant.USER_TOKEN_CLAIM, userToken.serialize());
    }
}
