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
package com.webank.wedpr.components.api.credential.core.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.webank.wedpr.common.config.WeDPRCommonConfig;
import com.webank.wedpr.common.utils.NoValueInCacheException;
import com.webank.wedpr.components.api.credential.core.CredentialCache;
import com.webank.wedpr.components.api.credential.dao.ApiCredentialDO;
import com.webank.wedpr.components.api.credential.dao.ApiCredentialMapper;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemoryCredentialCache implements CredentialCache {
    private static final Logger logger = LoggerFactory.getLogger(MemoryCredentialCache.class);

    private final ApiCredentialMapper credentialMapper;
    private final CredentialToolkit credentialToolkit;

    // accessID => Credential
    private LoadingCache<String, ApiCredentialDO> cache =
            CacheBuilder.newBuilder()
                    .maximumSize(WeDPRCommonConfig.getAuthCacheSize()) // the cache size
                    .expireAfterWrite(
                            WeDPRCommonConfig.getAuthCacheExpireTime(),
                            TimeUnit.MINUTES) // default 30min
                    .build(
                            new CacheLoader<String, ApiCredentialDO>() {
                                @Override
                                public ApiCredentialDO load(String accessKeyID)
                                        throws NoValueInCacheException {
                                    return fetchCredential(accessKeyID);
                                }
                            });

    public MemoryCredentialCache(
            ApiCredentialMapper credentialMapper, CredentialToolkit credentialToolkit) {
        this.credentialMapper = credentialMapper;
        this.credentialToolkit = credentialToolkit;
    }

    private ApiCredentialDO loadCache(String accessKeyID) {
        try {
            return cache.get(accessKeyID);
        } catch (Exception e) {
            logger.warn("get {} failed for ", accessKeyID, e);
            return null;
        }
    }

    @Override
    public ApiCredentialDO getAccessKey(String accessKeyID) {
        return loadCache(accessKeyID);
    }

    public ApiCredentialDO fetchCredential(String accessKeyID) throws NoValueInCacheException {
        try {
            ApiCredentialDO condition = new ApiCredentialDO(true);
            condition.setAccessKeyID(accessKeyID);
            List<ApiCredentialDO> result = this.credentialMapper.queryCredentials(condition);
            if (result == null || result.isEmpty()) {
                throw new NoValueInCacheException("accessKeyID " + accessKeyID + " not exists!");
            }
            credentialToolkit.decryptCredential(result.get(0));
            return result.get(0);
        } catch (Exception e) {
            logger.warn("fetchCredential exception, accessKeyID: {}, error: ", accessKeyID, e);
            throw new NoValueInCacheException(
                    "fetchCredential exception for accessKeyID: "
                            + accessKeyID
                            + ", error: "
                            + e.getLocalizedMessage());
        }
    }
}
