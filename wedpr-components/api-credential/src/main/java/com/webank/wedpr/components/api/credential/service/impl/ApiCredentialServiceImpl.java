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

package com.webank.wedpr.components.api.credential.service.impl;

import com.webank.wedpr.components.api.credential.core.impl.CredentialToolkit;
import com.webank.wedpr.components.api.credential.dao.ApiCredentialDO;
import com.webank.wedpr.components.api.credential.dao.ApiCredentialMapper;
import com.webank.wedpr.components.api.credential.service.ApiCredentialService;
import com.webank.wedpr.components.crypto.CryptoToolkit;
import com.webank.wedpr.components.crypto.CryptoToolkitFactory;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApiCredentialServiceImpl implements ApiCredentialService {

    private CryptoToolkit cryptoToolkit;
    @Autowired private ApiCredentialMapper credentialMapper;

    private CredentialToolkit credentialToolkit;

    @PostConstruct
    public void init() throws Exception {
        this.cryptoToolkit = CryptoToolkitFactory.build();
        this.credentialToolkit = new CredentialToolkit(cryptoToolkit);
    }
    /**
     * apply for new credential
     *
     * @param user the user to apply for the credential
     * @return the id
     */
    @Override
    public String applyForCredential(String user, ApiCredentialDO credential) throws Exception {
        credentialToolkit.initCredential(user, credential);
        this.credentialMapper.insertCredential(credential);
        return credential.getId();
    }

    /**
     * query the credentials by condition
     *
     * @param user the request user
     * @param condition the condition
     * @return the queried result
     */
    @Override
    public List<ApiCredentialDO> queryCredentials(String user, ApiCredentialDO condition)
            throws Exception {
        condition.setOwner(user);
        List<ApiCredentialDO> result = this.credentialMapper.queryCredentials(condition);
        this.credentialToolkit.decryptCredentials(result);
        return result;
    }

    /**
     * delete the specified credential
     *
     * @param user the request user
     * @param id the credential to delete
     * @return success/failed
     */
    @Override
    public boolean deleteCredential(String user, ApiCredentialDO condition) {
        condition.setOwner(user);
        return (this.credentialMapper.deleteCredentialByCondition(condition) > 0);
    }

    /**
     * update the credential state by condition
     *
     * @param user the request user
     * @param condition the update condition
     * @return success/failed
     */
    @Override
    public boolean updateCredential(String user, ApiCredentialDO condition) {
        condition.setOwner(user);
        return (this.credentialMapper.updateCredential(condition) > 0);
    }
}
