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

package com.webank.wedpr.adm.main;

import com.webank.wedpr.components.initializer.WeDPRApplication;
import com.webank.wedpr.components.leader.election.LeaderElection;
import com.webank.wedpr.components.publish.sync.PublishQuartzJob;
import com.webank.wedpr.components.quartz.config.QuartzBindJobConfig;
import com.webank.wedpr.components.sync.ResourceSyncer;
import com.webank.wedpr.core.utils.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"com.webank"})
public class AdmServiceApplication {
    private static final Logger logger = LoggerFactory.getLogger(AdmServiceApplication.class);

    public static void main(String[] args) throws Exception {
        System.out.println("start AdmServiceApplication");
        long startT = System.currentTimeMillis();
        WeDPRApplication.main(args, "PPCS-MODELADM");
        ResourceSyncer resourceSyncer =
                WeDPRApplication.getApplicationContext().getBean(ResourceSyncer.class);
        logger.info("start resourceSyncer");
        resourceSyncer.start();
        logger.info("start resourceSyncer success");
        // Note: must start leaderElection after the resourceSyncer started
        LeaderElection leaderElection =
                WeDPRApplication.getApplicationContext().getBean(LeaderElection.class);
        logger.info("start leaderElection");
        leaderElection.start();
        logger.info("start leaderElection success");
        startQuartz();
        System.out.println(
                "PPCS-MODELADM: start AdmServiceApplication success, timecost: "
                        + (System.currentTimeMillis() - startT)
                        + " ms.");
    }

    public static void startQuartz() throws Exception {
        QuartzBindJobConfig quartzBindJobConfig =
                WeDPRApplication.getApplicationContext().getBean(QuartzBindJobConfig.class);
        logger.info("Register Quartz job for service-publish...");
        quartzBindJobConfig.registerQuartzJob(
                Constant.DEFAULT_JOB_GROUP,
                "ServicePublishQuartzJob",
                "ServicePublishQuartzJob",
                PublishQuartzJob.class);
        logger.info("Register Quartz job for service-publish success");
        logger.info("Start Quartz...");
        quartzBindJobConfig.start();
        logger.info("Start Quartz success");
    }
}
