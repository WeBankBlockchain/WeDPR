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
package com.webank.wedpr.components.api.credential.core;

import com.webank.wedpr.core.config.WeDPRConfig;

public class CredentialConfig {
    private static Integer SIGNAUTURE_EXPIRATION_TIME =
            WeDPRConfig.apply("wedpr.credential.signature.expiration.minute", 10);

    public static Integer getSignautureExpirationTimeSeconds() {
        return SIGNAUTURE_EXPIRATION_TIME * 60;
    }
}
