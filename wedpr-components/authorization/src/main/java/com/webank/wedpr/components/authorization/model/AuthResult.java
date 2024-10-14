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

import com.webank.wedpr.common.config.WeDPRCommonConfig;
import com.webank.wedpr.common.utils.Common;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.common.utils.WeDPRException;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthResult {
    private static final Logger logger = LoggerFactory.getLogger(AuthResult.class);

    public static class AuthResultDetail {
        private String result;
        private AuthResultStatus authResultStatus;
        private String detail;
        private String time = Common.getCurrentTime();

        public AuthResultDetail() {}

        public AuthResultDetail(AuthResultStatus authResultStatus) {
            setAuthResultStatus(authResultStatus);
        }

        public void check() throws WeDPRException {
            Common.requireNonEmpty("result", result);
            if (authResultStatus == null) {
                throw new WeDPRException(
                        "Invalid authResult " + result + " , must be Agree or Reject!");
            }
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            if (StringUtils.isBlank(time)) {
                return;
            }
            this.time = time;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
            this.authResultStatus = AuthResultStatus.deserialize(result);
        }

        public AuthResultStatus getAuthResultStatus() {
            return authResultStatus;
        }

        public void setAuthResultStatus(AuthResultStatus authResult) {
            this.authResultStatus = authResult;
            if (this.authResultStatus == null) {
                return;
            }
            this.result = this.authResultStatus.getName();
        }

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }

        @Override
        public String toString() {
            return "AuthResultDetail{"
                    + "authResultStatus="
                    + authResultStatus
                    + ", detail='"
                    + detail
                    + '\''
                    + '}';
        }
    }

    public static class AuthNodeResult extends AuthChain.AuthNode {
        // the result
        private AuthResultDetail result;

        public AuthResultDetail getResult() {
            return result;
        }

        public void setResult(AuthResultDetail result) {
            this.result = result;
        }

        public AuthNodeResult() {}

        public AuthNodeResult(AuthChain.AuthNode authNode, AuthResultDetail result) {
            this.name = authNode.name;
            this.cnName = authNode.cnName;
            this.agency = authNode.agency;
            this.result = result;
        }
    }

    protected List<AuthNodeResult> resultList = new ArrayList<>();

    public List<AuthNodeResult> getResultList() {
        return resultList;
    }

    public void setResultList(List<AuthNodeResult> resultList) {
        if (resultList == null) {
            return;
        }
        this.resultList = resultList;
    }

    @SneakyThrows(WeDPRException.class)
    public void updateResult(
            AuthChain authChain, String authorizer, AuthResultDetail authResultDetail) {
        if (!authChain.getAuthorizerToAuthNode().containsKey(WeDPRCommonConfig.getAgency())
                || !authChain
                        .getAuthorizerToAuthNode()
                        .get(WeDPRCommonConfig.getAgency())
                        .containsKey(authorizer)) {
            logger.error(
                    "updateAuthResult error for invalid authorizer, authorizer: {}, result: {}",
                    authorizer,
                    authResultDetail.toString());
            throw new WeDPRException("updateResult failed for invalid authorizer " + authorizer);
        }
        resultList.add(
                new AuthNodeResult(
                        authChain
                                .getAuthorizerToAuthNode()
                                .get(WeDPRCommonConfig.getAgency())
                                .get(authorizer),
                        authResultDetail));
    }

    @SneakyThrows(Exception.class)
    public String serialize() {
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
    }

    @SneakyThrows(Exception.class)
    public static AuthResult deserialize(String authResult) {
        if (StringUtils.isBlank(authResult)) {
            return null;
        }
        return ObjectMapperFactory.getObjectMapper().readValue(authResult, AuthResult.class);
    }
}
