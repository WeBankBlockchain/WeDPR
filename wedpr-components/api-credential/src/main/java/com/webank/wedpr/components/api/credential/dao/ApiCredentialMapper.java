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

package com.webank.wedpr.components.api.credential.dao;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ApiCredentialMapper {

    /**
     * insert new credential
     *
     * @param credential the credential to insert
     * @return the inserted count
     */
    public Integer insertCredential(@Param("credential") ApiCredentialDO credential);

    /**
     * update the credential
     *
     * @param credential the credential to update
     * @return the updated count
     */
    public Integer updateCredential(@Param("credential") ApiCredentialDO credential);

    /**
     * delete the credential by condition
     *
     * @param condition the condition
     * @return the deleted count
     */
    public Integer deleteCredentialByCondition(@Param("condition") ApiCredentialDO condition);

    public List<ApiCredentialDO> queryCredentials(@Param("condition") ApiCredentialDO condition);
}
