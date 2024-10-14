/*
 * Copyright 2017-2025 [webank-wedpr]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 */

package com.webank.wedpr.components.token.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.webank.wedpr.common.protocol.RequestAuthType;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.token.auth.model.HeaderInfo;
import com.webank.wedpr.components.token.auth.model.TokenContents;
import com.webank.wedpr.components.token.auth.model.UserToken;
import com.webank.wedpr.components.token.auth.utils.SecurityUtils;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TokenUtils {
    private static final Logger logger = LoggerFactory.getLogger(TokenUtils.class);
    public static final String USER_TOKEN_CLAIM = "user";

    // generate the jwt token
    public static String generateJWTToken(
            HeaderInfo headerInfo, TokenContents tokenContents, String secret, long expireTimeMs)
            throws WeDPRException {
        try {
            // set the expiry time
            Date expireDate = new Date(System.currentTimeMillis() + expireTimeMs);
            JWTCreator.Builder builder =
                    JWT.create().withHeader(headerInfo.toDict()).withExpiresAt(expireDate);
            for (String key : tokenContents.getTokenContents().keySet()) {
                logger.debug("generateJWTToken: set {}", key);
                builder.withClaim(key, tokenContents.getTokenContents().get(key));
            }
            return builder.sign(SecurityUtils.getHMacAlgorithm(headerInfo.getAlg(), secret));
        } catch (Exception e) {
            logger.error(
                    "generateJWTToken failed, content: {}, error: ", tokenContents.toString(), e);
            throw new WeDPRException("generateJWTToken for " + e.getMessage(), e);
        }
    }

    // verify the jwt token
    public static void verify(String algorithmType, String secret, String token)
            throws WeDPRException {
        Algorithm algorithm = SecurityUtils.getHMacAlgorithm(algorithmType, secret);
        JWTVerifier verifier = JWT.require(algorithm).build();
        verifier.verify(token);
    }

    // get information from the jwt-token
    public static TokenContents getJWTTokenContent(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            Map<String, Claim> claimMap = jwt.getClaims();
            TokenContents tokenContents = new TokenContents();
            for (String tokenKey : claimMap.keySet()) {
                tokenContents.getTokenContents().put(tokenKey, claimMap.get(tokenKey).asString());
            }
            return tokenContents;
        } catch (Exception e) {
            logger.warn("getTokenContent exception, token: {}, error: ", token, e);
            return null;
        }
    }

    public static UserToken getLoginUser(HttpServletRequest request) throws WeDPRException {
        // get the user from the request
        try {
            RequestAuthType authType =
                    RequestAuthType.deserialize(
                            request.getHeader(Constant.REQUEST_AUTH_TYPE_FIELD));
            switch (authType) {
                    // the jwt auth
                case JWT:
                    {
                        String token = request.getHeader(Constant.TOKEN_FIELD);
                        if (StringUtils.isBlank(token)) {
                            throw new WeDPRException("getLoginUser failed for no token set!");
                        }
                        TokenContents tokenContents = getJWTTokenContent(token);
                        if (tokenContents == null) {
                            throw new WeDPRException("getLoginUser failed for no token set!");
                        }
                        return tokenContents.getUserToken();
                    }
                    // the api auth
                case ApiSignature:
                    {
                        return UserToken.deserialize(
                                request.getHeader(Constant.REQUEST_USER_TOKEN_FIELD));
                    }
                default:
                    {
                        logger.warn(
                                "getLoginUser failed for unsupported authType: {}",
                                authType.getType());
                        return null;
                    }
            }
        } catch (Exception e) {
            logger.warn("getLoginUser failed for " + e);
            throw new WeDPRException(e);
        }
    }

    public static void responseToClient(
            HttpServletResponse response, String message, int httpStatus) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(httpStatus);
        response.getWriter().write(message);
    }
}
