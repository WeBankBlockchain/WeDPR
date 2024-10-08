/** Copyright (C) @2014-2022 Webank */
package com.webank.wedpr.components.quartz.config;

import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class QuartzBindJobConfig {
    @Autowired private QuartzJobFactory quartzJobFactory;
    @Autowired private Scheduler scheduler;

    @Value("${quartz-cron-report-job:0/2 * * * * ? *}")
    private String quartzCronReportJob;

    @PostConstruct
    public void init() throws SchedulerException {
        scheduler.setJobFactory(quartzJobFactory);
    }

    public void start() throws Exception {
        scheduler.start();
    }

    public void registerQuartzJob(
            String jobGroup,
            String jobName,
            String jobDesc,
            Class<? extends org.quartz.Job> jobClass)
            throws Exception {
        JobDetail jobDetail =
                JobBuilder.newJob(jobClass)
                        .withIdentity(jobName, jobGroup)
                        .withDescription(jobDesc)
                        .build();
        CronScheduleBuilder cronScheduleBuilder =
                CronScheduleBuilder.cronSchedule(quartzCronReportJob);
        CronTrigger cronTrigger =
                TriggerBuilder.newTrigger()
                        .withIdentity(jobName + "Trigger", jobGroup)
                        .withSchedule(cronScheduleBuilder)
                        .build();
        JobKey jobKey = jobDetail.getKey();
        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
        }
        scheduler.scheduleJob(jobDetail, cronTrigger);
    }
}
