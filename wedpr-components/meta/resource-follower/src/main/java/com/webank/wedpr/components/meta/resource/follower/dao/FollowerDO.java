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

package com.webank.wedpr.components.meta.resource.follower.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.webank.wedpr.common.utils.TimeRange;
import com.webank.wedpr.components.uuid.generator.WeDPRUuidGenerator;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FollowerDO extends TimeRange {
    public enum FollowerType {
        AUTH_FOLLOWER("auth_follower"),
        AUTH_AUDITOR("auth_auditor"),
        JOB("job");

        private final String type;

        FollowerType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public static FollowerType deserialize(String type) {
            if (StringUtils.isBlank(type)) {
                return null;
            }
            for (FollowerType followerType : FollowerType.values()) {
                if (followerType.type.compareToIgnoreCase(type) == 0) {
                    return followerType;
                }
            }
            return null;
        }
    }

    private String id = WeDPRUuidGenerator.generateID();
    private String userName;
    private String agency;
    private String resourceID;
    private String followerType;
    @JsonIgnore private transient FollowerType type;
    private String createTime;
    private String lastUpdateTime;

    public FollowerDO() {}

    public FollowerDO(boolean emptyID) {
        if (emptyID) {
            this.id = "";
        }
    }

    public FollowerDO(String followerUser, String agency, String followerType) {
        setUserName(followerUser);
        setAgency(agency);
        setFollowerType(followerType);
    }

    public FollowerDO(String followerUser, String agency, String resourceID, String followerType) {
        setUserName(followerUser);
        setAgency(agency);
        setResourceID(resourceID);
        setFollowerType(followerType);
    }

    public void setFollowerType(String followerType) {
        this.followerType = followerType;
        this.type = FollowerType.deserialize(this.followerType);
    }

    public void setType(FollowerType type) {
        this.type = type;
        if (this.type == null) {
            return;
        }
        this.followerType = type.getType();
    }
}
