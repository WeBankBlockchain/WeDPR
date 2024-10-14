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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.webank.wedpr.common.utils.TimeRange;
import com.webank.wedpr.components.api.credential.core.impl.CredentialStatus;
import com.webank.wedpr.components.uuid.generator.WeDPRUuidGenerator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class ApiCredentialDO extends TimeRange {
    private String id = WeDPRUuidGenerator.generateID();
    private String accessKeyID;
    private String accessKeySecret;
    private String owner;
    private String status;
    private String desc;
    private String createTime;
    private String lastUpdateTime;

    @JsonIgnore private CredentialStatus credentialStatus;

    public ApiCredentialDO() {}

    public ApiCredentialDO(String id) {
        setId(id);
    }

    public ApiCredentialDO(boolean resetID) {
        if (resetID) {
            this.id = null;
        }
    }

    public ApiCredentialDO(String accessKeyID, String user, String status) {
        setAccessKeyID(accessKeyID);
        setOwner(user);
        this.owner = user;
        setStatus(status);
    }

    public void setStatus(String status) {
        this.status = status;
        this.credentialStatus = CredentialStatus.deserialize(status);
    }

    public void setCredentialStatus(CredentialStatus credentialStatus) {
        this.credentialStatus = credentialStatus;
        if (this.credentialStatus != null) {
            this.status = this.credentialStatus.getStatus();
        }
    }
}
