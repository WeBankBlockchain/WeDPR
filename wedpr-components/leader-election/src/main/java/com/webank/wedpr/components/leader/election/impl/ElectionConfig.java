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

package com.webank.wedpr.components.leader.election.impl;

import com.webank.wedpr.components.leader.election.LeaderElection;
import com.webank.wedpr.components.meta.sys.config.dao.SysConfigDO;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElectionConfig {
    private static final Logger logger = LoggerFactory.getLogger(ElectionConfig.class);
    // 1min
    private static final long DEFAULT_EXPIRE_INTERVAL_DELTA_TIME_SECONDS = 60;
    // the key
    private final String key;
    // the member id
    private final String memberID;
    private String leaderID = null;
    private Boolean isLeader = false;

    private final long keepAliveIntervalSeconds;
    private final long expireTimeSeconds;

    private List<LeaderElection.CampaignHandler> campaignHandlers = new ArrayList<>();

    public ElectionConfig(
            String key, String memberID, long keepAliveIntervalSeconds, long expireTimeSeconds) {
        this.key = key;
        this.memberID = memberID;
        this.keepAliveIntervalSeconds = keepAliveIntervalSeconds;
        if (expireTimeSeconds > keepAliveIntervalSeconds) {
            this.expireTimeSeconds = expireTimeSeconds;
        } else {
            this.expireTimeSeconds =
                    this.keepAliveIntervalSeconds + DEFAULT_EXPIRE_INTERVAL_DELTA_TIME_SECONDS;
        }
        logger.info(
                "create ElectionConfig, key: {}, member: {}, keep-alive-interval(seconds): {}, expire-time(seconds): {}",
                key,
                memberID,
                this.keepAliveIntervalSeconds,
                this.expireTimeSeconds);
    }

    public synchronized void resetConfig(boolean success, SysConfigDO leaderInfo) {
        if (leaderInfo != null) {
            this.leaderID = leaderInfo.getConfigValue();
        } else {
            this.leaderID = null;
        }
        if (this.isLeader.equals(success)) {
            return;
        }
        this.isLeader = success;
        logger.info("resetConfig: {}", toString());
        // trigger the callback
        for (LeaderElection.CampaignHandler handler : campaignHandlers) {
            handler.onCampaign(success, this.leaderID);
        }
    }

    public String getKey() {
        return key;
    }

    public String getMemberID() {
        return memberID;
    }

    public List<LeaderElection.CampaignHandler> getCampaignHandlers() {
        return campaignHandlers;
    }

    public synchronized void addCampaignHandler(LeaderElection.CampaignHandler campaignHandler) {
        this.campaignHandlers.add(campaignHandler);
    }

    public long getKeepAliveIntervalSeconds() {
        return this.keepAliveIntervalSeconds;
    }

    public String getLeaderID() {
        return leaderID;
    }

    public Boolean getIsLeader() {
        return isLeader;
    }

    public void setLeaderID(String leaderID) {
        this.leaderID = leaderID;
    }

    public void setLeader(Boolean leader) {
        isLeader = leader;
    }

    public long getExpireTimeSeconds() {
        return this.expireTimeSeconds;
    }

    @Override
    public String toString() {
        return "ElectionConfig{"
                + "key='"
                + key
                + '\''
                + ", memberID='"
                + memberID
                + '\''
                + ", leaderID='"
                + leaderID
                + '\''
                + ", isLeader="
                + isLeader
                + '}';
    }
}
