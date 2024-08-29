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

package com.webank.wedpr.components.api.credential.service;

import com.webank.wedpr.components.api.credential.dao.ApiCredentialDO;
import java.util.List;

public interface ApiCredentialService {
    /**
     * apply for new credential
     *
     * @param user the user to apply for the credential
     * @return the id
     */
    String applyForCredential(String user, ApiCredentialDO credential) throws Exception;

    /**
     * query the credentials by condition
     *
     * @param user the request user
     * @param condition the condition
     * @return the queried result
     */
    List<ApiCredentialDO> queryCredentials(String user, ApiCredentialDO condition);

    /**
     * delete the specified credential
     *
     * @param user the request user
     * @param id the credential to delete
     * @return success/failed
     */
    boolean deleteCredential(String user, String id);

    /**
     * update the credential state by condition
     *
     * @param user the request user
     * @param condition the update condition
     * @return success/failed
     */
    boolean updateCredential(String user, ApiCredentialDO condition);
}
