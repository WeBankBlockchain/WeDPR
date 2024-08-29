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
package com.webank.wedpr.components.security.cache.impl;

import com.webank.wedpr.core.config.WeDPRConfig;

public class CacheConfig {
    private static Integer USER_CACHE_SIZE = WeDPRConfig.apply("wedpr.user.cache.size", 100000);

    public static Integer getUserCacheSize() {
        return USER_CACHE_SIZE;
    }
}
