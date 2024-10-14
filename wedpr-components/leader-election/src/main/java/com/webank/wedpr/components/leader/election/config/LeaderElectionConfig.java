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

package com.webank.wedpr.components.leader.election.config;

import com.webank.wedpr.common.config.WeDPRConfig;
import com.webank.wedpr.components.leader.election.LeaderElection;
import com.webank.wedpr.components.leader.election.impl.ElectionConfig;
import com.webank.wedpr.components.leader.election.impl.LeaderElectionImpl;
import com.webank.wedpr.components.meta.sys.config.WeDPRSysConfig;
import com.webank.wedpr.components.meta.sys.config.loader.SysConfigLoader;
import com.webank.wedpr.components.uuid.generator.WeDPRUuidGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@AutoConfigureAfter(SysConfigLoader.class)
public class LeaderElectionConfig {
    private static final Logger logger = LoggerFactory.getLogger(LeaderElectionConfig.class);

    @Autowired private WeDPRSysConfig weDPRSysConfig;

    @Bean(name = "leaderElection")
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    @ConditionalOnMissingBean
    public LeaderElection leaderElection() {
        // create the ElectionConfig
        String key = WeDPRConfig.apply("wedpr.leader.election.key", "leader");
        String memberID =
                WeDPRConfig.apply(
                        "wedpr.leader.election.member.id", WeDPRUuidGenerator.generateID());
        // 4min
        Integer keepAliveIntervalSeconds =
                WeDPRConfig.apply("wedpr.leader.election.keep.alive.seconds", 30);
        // 5min
        Integer expireTimeSeconds = WeDPRConfig.apply("wedpr.leader.election.expire.seconds", 60);
        logger.info(
                "create leaderElection, key: {}, member: {}, keepAliveSeconds: {}, expireSeconds: {}",
                key,
                memberID,
                keepAliveIntervalSeconds,
                expireTimeSeconds);
        LeaderElectionImpl leaderElectionObj =
                new LeaderElectionImpl(
                        new ElectionConfig(
                                key, memberID, keepAliveIntervalSeconds, expireTimeSeconds),
                        weDPRSysConfig);
        return leaderElectionObj;
    }
}
