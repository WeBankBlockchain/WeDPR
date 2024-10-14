/** Copyright (C) @2014-2022 Webank */
package com.webank.wedpr.components.report.config;

import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.components.quartz.config.QuartzBindJobConfig;
import com.webank.wedpr.components.report.job.ReportQuartzJob;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

@Configuration
public class ApplicationListenerConfig implements ApplicationListener<ContextRefreshedEvent> {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired private QuartzBindJobConfig quartzBindJobConfig;

    @SneakyThrows(Exception.class)
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        logger.info("Register Quartz job for report...");
        quartzBindJobConfig.registerQuartzJob(
                Constant.DEFAULT_JOB_GROUP,
                "ReportQuartzJob",
                "Quartz-ReportQuartzJob",
                ReportQuartzJob.class);
        logger.info("Register Quartz job for report success");
    }
}
