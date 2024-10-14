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
package com.webank.wedpr.components.security.filter;

import com.webank.wedpr.common.protocol.RequestAuthType;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.api.credential.core.CredentialVerifier;
import com.webank.wedpr.components.api.credential.dao.ApiCredentialDO;
import com.webank.wedpr.components.security.cache.UserCache;
import com.webank.wedpr.components.token.auth.TokenUtils;
import com.webank.wedpr.components.token.auth.model.UserToken;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.StringUtils;

public class APISignatureAuthFilter extends BasicAuthenticationFilter {
    private final CredentialVerifier credentialVerifier;
    private final UserCache userCache;

    public APISignatureAuthFilter(
            AuthenticationManager authenticationManager,
            CredentialVerifier credentialVerifier,
            UserCache userCache) {
        super(authenticationManager);
        this.credentialVerifier = credentialVerifier;
        this.userCache = userCache;
    }

    @SneakyThrows
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
        try {
            // auth by token
            if (!StringUtils.isEmpty(request.getHeader(Constant.TOKEN_FIELD))) {
                chain.doFilter(request, response);
                return;
            }
            ApiCredentialDO credential = this.credentialVerifier.verify(request);
            UserToken userToken = userCache.getUserToken(credential.getOwner());
            if (userToken == null) {
                throw new WeDPRException(Constant.WEDPR_FAILED, "用户不存在");
            }
            // set the accessKeyID
            userToken.setAccessKeyID(credential.getAccessKeyID());
            HeaderMapRequestWrapper requestWrapper = new HeaderMapRequestWrapper(request);
            // set the auth type
            requestWrapper.addHeader(
                    Constant.REQUEST_AUTH_TYPE_FIELD, RequestAuthType.ApiSignature.getType());
            // set the userToken information
            requestWrapper.addHeader(Constant.REQUEST_USER_TOKEN_FIELD, userToken.serialize());
            // set the response information
            response.addHeader(
                    Constant.REQUEST_AUTH_TYPE_FIELD, RequestAuthType.ApiSignature.getType());
            response.addHeader(Constant.REQUEST_USER_TOKEN_FIELD, userToken.serialize());
            chain.doFilter(requestWrapper, response);
        } catch (Exception e) {
            logger.warn("APISignatureAuthFilter exception, error: ", e);
            TokenUtils.responseToClient(response, e.getMessage(), HttpServletResponse.SC_FORBIDDEN);
        }
    }
}
