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

import com.webank.wedpr.common.utils.ThreadPoolService;
import com.webank.wedpr.components.leader.election.LeaderElection;
import com.webank.wedpr.components.meta.sys.config.WeDPRSysConfig;
import com.webank.wedpr.components.meta.sys.config.dao.SysConfigDO;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeaderElectionImpl implements LeaderElection {
    private static final Logger logger = LoggerFactory.getLogger(LeaderElectionImpl.class);
    private final ElectionConfig electionConfig;
    private final WeDPRSysConfig weDPRSysConfig;
    private boolean running = false;
    private final ScheduledExecutorService keepAliveTimer = new ScheduledThreadPoolExecutor(1);

    public LeaderElectionImpl(ElectionConfig electionConfig, WeDPRSysConfig weDPRSysConfig) {
        this.electionConfig = electionConfig;
        this.weDPRSysConfig = weDPRSysConfig;
        // insert the key
        this.weDPRSysConfig.allocateKey(electionConfig.getKey(), "");
    }

    // register the campaign handler
    @Override
    public void registerOnCampaignHandler(LeaderElection.CampaignHandler handler) {
        this.electionConfig.addCampaignHandler(handler);
    }

    // start
    @Override
    public void start() {
        if (running) {
            logger.info(
                    "The leader-election worker has already been started, config: {}",
                    electionConfig.toString());
            return;
        }
        logger.info("Start the leader-election worker, config: {}", electionConfig.toString());
        running = true;
        tryCampaignToLeader();
        keepAliveTimer.scheduleAtFixedRate(
                this::keepAlive,
                0,
                this.electionConfig.getKeepAliveIntervalSeconds(),
                TimeUnit.SECONDS);
    }

    // stop
    @Override
    public void stop() {
        if (!running) {
            logger.info(
                    "The leader-election worker has already been stopped, config: {}",
                    electionConfig.toString());
            return;
        }
        logger.info("Stop the leader-election, config: {}", electionConfig.toString());
        running = false;
        ThreadPoolService.stopThreadPool(this.keepAliveTimer);
    }

    @Override
    public Boolean isLeader() {
        return electionConfig.getIsLeader();
    }

    @Override
    public String leaderID() {
        return electionConfig.getLeaderID();
    }

    public boolean isRunning() {
        return running;
    }

    private void tryCampaignToLeader() {
        int retryTime = 0;
        boolean campaignFinished = false;
        do {
            try {
                boolean success =
                        this.weDPRSysConfig.resetConfig(
                                electionConfig.getKey(),
                                electionConfig.getMemberID(),
                                electionConfig.getExpireTimeSeconds());
                // campaign as the leader, election finished
                if (success) {
                    SysConfigDO leaderInfo = new SysConfigDO();
                    leaderInfo.setConfigValue(electionConfig.getMemberID());
                    electionConfig.resetConfig(true, leaderInfo);
                    campaignFinished = true;
                    logger.info(
                            "tryCampaignToLeader: AS Leader, config: {}",
                            electionConfig.toString());
                    break;
                }
                // campaign failed, try to fetch the leader
                SysConfigDO leaderConfig =
                        this.weDPRSysConfig.getActiveConfig(
                                electionConfig.getKey(), electionConfig.getExpireTimeSeconds());
                // as the follower, election finished
                if (leaderConfig != null) {
                    electionConfig.resetConfig(false, leaderConfig);
                    campaignFinished = true;
                    logger.info(
                            "tryCampaignToLeader: AS Follower, config: {}",
                            electionConfig.toString());
                    break;
                }
                retryTime += 1;
                // no leader, continue to campaign after 10ms
                Thread.sleep(10);
            } catch (Exception e) {
                logger.warn(
                        "tryCampaignToLeader exception, config: {}, error: ",
                        electionConfig.toString(),
                        e);
                break;
            }
        } while (true);
        logger.info(
                "tryCampaignToLeader, config: {}, retryTime: {}, finished: {}",
                electionConfig.toString(),
                retryTime,
                campaignFinished);
    }

    // refresh the status
    private void refreshStatus() {
        // refresh success, the leader has not been changed
        if (this.weDPRSysConfig.refreshConfig(
                electionConfig.getKey(), electionConfig.getMemberID())) {
            return;
        }
        // refresh failed, update the leader information
        getLeader();
        logger.info(
                "leader election refresh failed, switch to follower, config: {}",
                electionConfig.toString());
    }

    private void getLeader() {
        SysConfigDO leaderInfo =
                this.weDPRSysConfig.getActiveConfig(
                        electionConfig.getKey(), electionConfig.getExpireTimeSeconds());
        if (leaderInfo != null
                && leaderInfo.getConfigValue().equals(electionConfig.getMemberID())) {
            electionConfig.resetConfig(true, leaderInfo);
        } else {
            electionConfig.resetConfig(false, leaderInfo);
        }
    }

    protected void keepAlive() {
        try {
            // the leader refresh the status
            if (electionConfig.getIsLeader()) {
                refreshStatus();
            } else {
                getLeader();
            }
            // with valid leader
            if (electionConfig.getLeaderID() != null) {
                return;
            }
            // with no leader, try to Campaign
            logger.info(
                    "leader election expired, tryCampaignToLeader, config: {}",
                    electionConfig.toString());
            tryCampaignToLeader();
        } catch (Exception e) {
            logger.warn("keepAlive exception, config: {}, error: ", electionConfig.toString(), e);
        }
    }
}
