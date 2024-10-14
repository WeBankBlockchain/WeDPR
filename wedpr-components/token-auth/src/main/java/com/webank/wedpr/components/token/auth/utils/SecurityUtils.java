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

package com.webank.wedpr.components.token.auth.utils;

import com.auth0.jwt.algorithms.Algorithm;
import com.webank.wedpr.common.utils.WeDPRException;

public class SecurityUtils {

    public static final String HMAC_HS256 = "HS256";
    public static final String HMAC_HS512 = "HS512";
    public static final String HMAC_HS384 = "HS384";

    public static Algorithm getHMacAlgorithm(String algorithmType, String secret)
            throws WeDPRException {
        if (algorithmType.compareToIgnoreCase(HMAC_HS256) == 0) {
            return Algorithm.HMAC256(secret);
        }
        if (algorithmType.compareToIgnoreCase(HMAC_HS512) == 0) {
            return Algorithm.HMAC512(secret);
        }
        if (algorithmType.compareToIgnoreCase(HMAC_HS384) == 0) {
            return Algorithm.HMAC384(secret);
        }
        throw new WeDPRException(
                "getHMacAlgorithm failed for not supported algorithm: " + algorithmType);
    }
}
