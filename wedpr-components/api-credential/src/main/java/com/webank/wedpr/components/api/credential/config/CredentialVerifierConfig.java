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
package com.webank.wedpr.components.api.credential.config;

import com.webank.wedpr.components.api.credential.core.CredentialVerifier;
import com.webank.wedpr.components.api.credential.core.impl.CredentialToolkit;
import com.webank.wedpr.components.api.credential.core.impl.CredentialVerifierImpl;
import com.webank.wedpr.components.api.credential.core.impl.MemoryCredentialCache;
import com.webank.wedpr.components.api.credential.dao.ApiCredentialMapper;
import com.webank.wedpr.components.crypto.CryptoToolkit;
import com.webank.wedpr.components.crypto.CryptoToolkitFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class CredentialVerifierConfig {
    private static final Logger logger = LoggerFactory.getLogger(CredentialVerifierConfig.class);
    @Autowired private ApiCredentialMapper credentialMapper;

    @Bean(name = "credentialVerifier")
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    @ConditionalOnMissingBean
    public CredentialVerifier credentialVerifier() throws Exception {
        CryptoToolkit cryptoToolkit = CryptoToolkitFactory.build();
        CredentialToolkit toolkit = new CredentialToolkit(cryptoToolkit);
        return new CredentialVerifierImpl(
                cryptoToolkit, new MemoryCredentialCache(credentialMapper, toolkit));
    }
}
