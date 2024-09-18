/** Copyright (C) @2014-2022 Webank */
package com.webank.wedpr.components.report.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.webank.wedpr.components.meta.sys.config.dao.SysConfigDO;
import com.webank.wedpr.components.meta.sys.config.dao.SysConfigMapper;
import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.project.dao.JobDatasetDO;
import com.webank.wedpr.components.project.dao.ProjectDO;
import com.webank.wedpr.components.project.dao.ProjectMapper;
import com.webank.wedpr.components.report.handler.*;
import com.webank.wedpr.core.config.WeDPRCommonConfig;
import com.webank.wedpr.core.protocol.ReportStatusEnum;
import com.webank.wedpr.core.protocol.TransportTopicEnum;
import com.webank.wedpr.core.utils.Constant;
import com.webank.wedpr.core.utils.ObjectMapperFactory;
import com.webank.wedpr.sdk.jni.transport.WeDPRTransport;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@DisallowConcurrentExecution
@Slf4j
public class ReportQuartzJob implements Job {
    @Autowired private ProjectMapper projectMapper;
    @Autowired private SysConfigMapper sysConfigMapper;

    @Autowired private WeDPRTransport weDPRTransport;

    @Override
    public void execute(JobExecutionContext context) {
        log.info("ReportQuartzJob run at:{}", LocalDateTime.now());
        try {
            doReport();
        } catch (Throwable e) {
            log.warn("ReportQuartzJob run error", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    private void doReport() {
        log.info("do report...");
        try {
            String adminAgency = WeDPRCommonConfig.getAdminAgency();
            reportProjectInfo(adminAgency);
            reportJobInfo(adminAgency);
            reportJobDatasteRelationInfo(adminAgency);
            reportSysConfig(adminAgency);
        } catch (Exception e) {
            log.warn("report error", e);
        }
    }

    private void reportSysConfig(String agency) throws JsonProcessingException {
        SysConfigReportMessageHandler sysConfigReportMessageHandler =
                new SysConfigReportMessageHandler(sysConfigMapper);
        List<SysConfigDO> sysConfigDOList = sysConfigMapper.queryAllConfig();
        byte[] payload = ObjectMapperFactory.getObjectMapper().writeValueAsBytes(sysConfigDOList);
        weDPRTransport.asyncSendMessageByAgency(
                TransportTopicEnum.SYS_CONFIG_REPORT.name(),
                agency,
                payload,
                0,
                WeDPRCommonConfig.getReportTimeout(),
                new ErrorCallback("reportSysConfig"),
                sysConfigReportMessageHandler);
    }

    private void reportJobDatasteRelationInfo(String agency) throws JsonProcessingException {
        JobDatasetRelationReportMessageHandler jobDatasetRelationReportMessageHandler =
                new JobDatasetRelationReportMessageHandler(projectMapper);
        JobDatasetDO jobDatasetDO = new JobDatasetDO();
        jobDatasetDO.setReportStatus(ReportStatusEnum.NO_REPORT.getReportStatus());
        jobDatasetDO.setLimitItems(Constant.DEFAULT_REPORT_PAGE_SIZE);
        List<JobDatasetDO> jobDatasetDOList = projectMapper.queryJobDatasetInfo(jobDatasetDO);
        byte[] payload = ObjectMapperFactory.getObjectMapper().writeValueAsBytes(jobDatasetDOList);
        weDPRTransport.asyncSendMessageByAgency(
                TransportTopicEnum.JOB_DATASET_REPORT.name(),
                agency,
                payload,
                0,
                WeDPRCommonConfig.getReportTimeout(),
                new ErrorCallback("reportSysConfig"),
                jobDatasetRelationReportMessageHandler);
    }

    private void reportJobInfo(String agency) throws JsonProcessingException {
        JobReportMessageHandler jobReportMessageHandler =
                new JobReportMessageHandler(projectMapper);
        JobDO jobDO = new JobDO();
        jobDO.setReportStatus(ReportStatusEnum.NO_REPORT.getReportStatus());
        jobDO.setLimitItems(Constant.DEFAULT_REPORT_PAGE_SIZE);
        List<JobDO> jobDOList = projectMapper.queryJobs(false, jobDO, null);
        byte[] payload = ObjectMapperFactory.getObjectMapper().writeValueAsBytes(jobDOList);
        weDPRTransport.asyncSendMessageByAgency(
                TransportTopicEnum.JOB_REPORT.name(),
                agency,
                payload,
                0,
                WeDPRCommonConfig.getReportTimeout(),
                new ErrorCallback("reportJobInfo"),
                jobReportMessageHandler);
    }

    private void reportProjectInfo(String agency) throws JsonProcessingException {
        ProjectReportMessageHandler projectReportMessageHandler =
                new ProjectReportMessageHandler(projectMapper);
        ProjectDO projectDO = new ProjectDO();
        projectDO.setReportStatus(ReportStatusEnum.NO_REPORT.getReportStatus());
        projectDO.setLimitItems(Constant.DEFAULT_REPORT_PAGE_SIZE);
        List<ProjectDO> projectDOList = projectMapper.queryProject(false, projectDO);
        byte[] payload = ObjectMapperFactory.getObjectMapper().writeValueAsBytes(projectDOList);
        weDPRTransport.asyncSendMessageByAgency(
                TransportTopicEnum.PROJECT_REPORT.name(),
                agency,
                payload,
                0,
                WeDPRCommonConfig.getReportTimeout(),
                new ErrorCallback("reportProjectInfo"),
                projectReportMessageHandler);
    }
}
