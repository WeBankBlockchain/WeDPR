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

package com.webank.wedpr.components.token.auth.demo;

import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.components.token.auth.TokenUtils;
import com.webank.wedpr.components.token.auth.model.GroupInfo;
import com.webank.wedpr.components.token.auth.model.HeaderInfo;
import com.webank.wedpr.components.token.auth.model.TokenContents;
import com.webank.wedpr.components.token.auth.model.UserToken;
import java.util.ArrayList;
import java.util.List;

public class TokenGenerator {
    public static void main(String[] args) throws Exception {
        String user = "zachma";
        if (args.length > 0) {
            user = args[0];
        }
        String secret = "123456";
        if (args.length > 1) {
            secret = args[1];
        }
        String groupId = "9159243068155909";
        if (args.length > 2) {
            groupId = args[2];
        }
        HeaderInfo headerInfo = new HeaderInfo();
        TokenContents tokenContents = new TokenContents();
        UserToken userToken = new UserToken();
        userToken.setUsername(user);
        userToken.setRoleName("admin_user");
        List<GroupInfo> groupInfos = new ArrayList<>();
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setGroupId(groupId);
        userToken.setGroupInfos(groupInfos);
        tokenContents.addTokenContents(Constant.USER_TOKEN_CLAIM, userToken.serialize());
        System.out.println("====== generate token for user: " + user + ", groupId: " + groupId);
        String token =
                TokenUtils.generateJWTToken(new HeaderInfo(), tokenContents, secret, 36000L * 1000);
        System.out.println("token: " + token);
    }
}
