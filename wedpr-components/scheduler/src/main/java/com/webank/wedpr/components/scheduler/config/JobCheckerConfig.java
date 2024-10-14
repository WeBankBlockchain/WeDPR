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

package com.webank.wedpr.components.scheduler.config;

import com.webank.wedpr.common.protocol.JobType;
import com.webank.wedpr.components.project.JobChecker;
import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.scheduler.executor.ExecutorParamChecker;
import com.webank.wedpr.components.scheduler.executor.impl.ml.MLExecutorParamChecker;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMetaBuilder;
import com.webank.wedpr.components.scheduler.executor.impl.mpc.MPCExecutorParamChecker;
import com.webank.wedpr.components.scheduler.executor.impl.pir.PirExecutorParamChecker;
import com.webank.wedpr.components.scheduler.executor.impl.psi.PSIExecutorParamChecker;
import lombok.Data;
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
@AutoConfigureAfter(FileMetaBuilderConfig.class)
@Data
public class JobCheckerConfig {
    private static final Logger logger = LoggerFactory.getLogger(JobCheckerConfig.class);
    @Autowired private FileMetaBuilder fileMetaBuilder;

    @Bean(name = "jobChecker")
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    @ConditionalOnMissingBean
    public JobChecker jobChecker() throws Exception {
        JobChecker jobChecker = new JobChecker();
        // register PSI job param checker
        registerJobChecker(jobChecker, new PSIExecutorParamChecker(fileMetaBuilder));
        logger.info("registerPSIJobChecker success");
        // register ML job param checker
        registerJobChecker(jobChecker, new MLExecutorParamChecker(fileMetaBuilder));
        logger.info("registerMLJobChecker success");

        registerJobChecker(jobChecker, new PirExecutorParamChecker());
        logger.info("registerPirJobChecker success");

        // register MPC job param checker
        registerMPCJobChecker(jobChecker, fileMetaBuilder);
        logger.info("registerMPCJobChecker success");

        return jobChecker;
    }

    protected void registerJobChecker(
            JobChecker jobChecker, ExecutorParamChecker executorParamChecker) {
        // register ML job param checker
        for (JobType jobType : executorParamChecker.getJobTypeList()) {
            jobChecker.registerJobCheckHandler(
                    jobType,
                    new JobChecker.JobCheckHandler() {
                        @Override
                        public Object checkAndParseParam(JobDO jobDO) throws Exception {
                            return executorParamChecker.checkAndParseJob(jobDO);
                        }
                    });
        }
    }

    public void registerMPCJobChecker(JobChecker jobChecker, FileMetaBuilder fileMetaBuilder) {
        MPCExecutorParamChecker mpcExecutorParamChecker = new MPCExecutorParamChecker();
        for (JobType jobType : mpcExecutorParamChecker.getJobTypeList()) {
            jobChecker.registerJobCheckHandler(
                    jobType,
                    new JobChecker.JobCheckHandler() {
                        @Override
                        public Object checkAndParseParam(JobDO jobDO) throws Exception {
                            return mpcExecutorParamChecker.checkAndParseJob(jobDO);
                        }
                    });
        }
    }
}
