package com.webank.wedpr.components.admin.transport;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.wedpr.common.config.WeDPRConfig;
import com.webank.wedpr.common.protocol.TransportComponentEnum;
import com.webank.wedpr.common.protocol.TransportTopicEnum;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.common.utils.ThreadPoolService;
import com.webank.wedpr.components.admin.entity.WedprJobDatasetRelation;
import com.webank.wedpr.components.admin.entity.WedprJobTable;
import com.webank.wedpr.components.admin.entity.WedprProjectTable;
import com.webank.wedpr.components.admin.service.WedprJobDatasetRelationService;
import com.webank.wedpr.components.admin.service.WedprJobTableService;
import com.webank.wedpr.components.admin.service.WedprProjectTableService;
import com.webank.wedpr.components.meta.sys.config.dao.SysConfigDO;
import com.webank.wedpr.components.meta.sys.config.dao.SysConfigMapper;
import com.webank.wedpr.components.transport.CommonErrorCallback;
import com.webank.wedpr.components.transport.message.JobDatasetReportResponse;
import com.webank.wedpr.components.transport.message.JobReportResponse;
import com.webank.wedpr.components.transport.message.ProjectReportResponse;
import com.webank.wedpr.components.transport.message.SysConfigReportResponse;
import com.webank.wedpr.sdk.jni.transport.IMessage;
import com.webank.wedpr.sdk.jni.transport.WeDPRTransport;
import com.webank.wedpr.sdk.jni.transport.handlers.MessageDispatcherCallback;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

/** Created by caryliao on 2024/9/4 15:03 */
@Service
@Slf4j
public class TopicSubscriber implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(TopicSubscriber.class);
    private static final String WORKER_NAME = "report";
    @Autowired private WeDPRTransport weDPRTransport;

    @Autowired private WedprProjectTableService wedprProjectTableService;
    @Autowired private WedprJobTableService wedprJobTableService;
    @Autowired private WedprJobDatasetRelationService wedprJobDatasetRelationService;
    @Autowired private SysConfigMapper sysConfigMapper;
    ThreadPoolService reportWorker =
            new ThreadPoolService(
                    WORKER_NAME, WeDPRConfig.apply("wedpr.thread.max.blocking.queue.size", 1000));

    @Override
    public void run(String... args) throws Exception {
        try {
            weDPRTransport.registerComponent(TransportComponentEnum.REPORT.name());
            logger.info(
                    "TopicSubscriber: registerComponent: {}", TransportComponentEnum.REPORT.name());
            subscribeProjectTopic();
            subscribeJobTopic();
            subscribeJobDatasetRelationTopic();
        } catch (Exception e) {
            log.warn("subscribe topic error", e);
        }
    }

    private void executeHandleReportSysConfig(
            IMessage message, List<SysConfigDO> sysConfigDOList, SysConfigReportResponse response) {
        List<String> configKeyList = new ArrayList<>();
        byte[] responsePayload = null;
        try {
            for (SysConfigDO sysConfigDO : sysConfigDOList) {
                String configKey = sysConfigDO.getConfigKey();
                SysConfigDO queriedSysConfigDO = sysConfigMapper.queryConfig(configKey);
                if (queriedSysConfigDO == null) {
                    sysConfigMapper.insertConfig(sysConfigDO);
                } else {
                    sysConfigMapper.updateConfig(sysConfigDO);
                }
                configKeyList.add(configKey);
            }
            response.setCode(Constant.WEDPR_SUCCESS);
            response.setMsg(Constant.WEDPR_SUCCESS_MSG);
            response.setConfigKeyList(configKeyList);
            log.info("report sys config ok, response:{}", response);
            log.info("report configKeyList size:{}", configKeyList.size());
            responsePayload = ObjectMapperFactory.getObjectMapper().writeValueAsBytes(response);
        } catch (JsonProcessingException e) {
            log.error("handle error", e);
            response.setCode(Constant.WEDPR_FAILED);
            response.setMsg("handle error" + e.getMessage());
        }
        IMessage.IMessageHeader messageHeader = message.getHeader();
        weDPRTransport.asyncSendResponse(
                messageHeader.getSrcNode(),
                messageHeader.getTraceID(),
                responsePayload,
                0,
                new CommonErrorCallback("asyncSendSysConfigResponse"));
    }

    private void subscribeJobDatasetRelationTopic() {
        weDPRTransport.registerTopicHandler(
                TransportTopicEnum.JOB_DATASET_REPORT.name(),
                new MessageDispatcherCallback() {
                    @Override
                    public void onMessage(IMessage message) {
                        log.info("receive job dataset relation report");
                        byte[] payload = message.getPayload();
                        List<WedprJobDatasetRelation> wedprJobDatasetRelationList =
                                new ArrayList<>();
                        JobDatasetReportResponse response = new JobDatasetReportResponse();
                        try {
                            wedprJobDatasetRelationList =
                                    ObjectMapperFactory.getObjectMapper()
                                            .readValue(
                                                    payload,
                                                    new TypeReference<
                                                            List<WedprJobDatasetRelation>>() {});
                        } catch (IOException e) {
                            log.warn("parse message error", e);
                            response.setCode(Constant.WEDPR_FAILED);
                            response.setMsg("parse message error" + e.getMessage());
                        }
                        log.info(
                                "report wedprJobDatasetRelationList:{}",
                                wedprJobDatasetRelationList);
                        List<WedprJobDatasetRelation> finalWedprJobDatasetRelationList =
                                wedprJobDatasetRelationList;
                        reportWorker
                                .getThreadPool()
                                .execute(
                                        () -> {
                                            executeHandleReportJobDatasetRelation(
                                                    message,
                                                    finalWedprJobDatasetRelationList,
                                                    response);
                                        });
                    }
                });
    }

    private void executeHandleReportJobDatasetRelation(
            IMessage message,
            List<WedprJobDatasetRelation> wedprJobDatasetRelationList,
            JobDatasetReportResponse response) {
        List<String> jobIdList = new ArrayList<>();
        byte[] responsePayload = null;
        try {
            for (WedprJobDatasetRelation wedprJobDatasetRelation : wedprJobDatasetRelationList) {
                String jobId = wedprJobDatasetRelation.getJobId();
                LambdaQueryWrapper<WedprJobDatasetRelation> lambdaQueryWrapper =
                        new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(WedprJobDatasetRelation::getJobId, jobId);
                WedprJobDatasetRelation queriedWedprJobDatasetRelation =
                        wedprJobDatasetRelationService.getOne(lambdaQueryWrapper);
                if (queriedWedprJobDatasetRelation == null) {
                    wedprJobDatasetRelationService.save(wedprJobDatasetRelation);
                } else {
                    wedprJobDatasetRelationService.update(
                            wedprJobDatasetRelation, lambdaQueryWrapper);
                }
                jobIdList.add(jobId);
            }
            response.setCode(Constant.WEDPR_SUCCESS);
            response.setMsg(Constant.WEDPR_SUCCESS_MSG);
            response.setJobIdList(jobIdList);
            log.info("report job dataset relation ok, response:{}", response);
            log.info("report jobIdList size:{}", jobIdList.size());
            responsePayload = ObjectMapperFactory.getObjectMapper().writeValueAsBytes(response);
        } catch (JsonProcessingException e) {
            log.error("handle error", e);
            response.setCode(Constant.WEDPR_FAILED);
            response.setMsg("handle error" + e.getMessage());
        }
        IMessage.IMessageHeader messageHeader = message.getHeader();
        weDPRTransport.asyncSendResponse(
                messageHeader.getSrcNode(),
                messageHeader.getTraceID(),
                responsePayload,
                0,
                new CommonErrorCallback("asyncSendResponseForDataset"));
    }

    private void subscribeJobTopic() {
        weDPRTransport.registerTopicHandler(
                TransportTopicEnum.JOB_REPORT.name(),
                new MessageDispatcherCallback() {
                    @Override
                    public void onMessage(IMessage message) {
                        log.info("receive job report");
                        byte[] payload = message.getPayload();
                        List<WedprJobTable> wedprJobTableList = null;
                        JobReportResponse response = new JobReportResponse();
                        try {
                            wedprJobTableList =
                                    ObjectMapperFactory.getObjectMapper()
                                            .readValue(
                                                    payload,
                                                    new TypeReference<List<WedprJobTable>>() {});
                        } catch (IOException e) {
                            log.warn("parse message error", e);
                            response.setCode(Constant.WEDPR_FAILED);
                            response.setMsg("parse message error" + e.getMessage());
                        }
                        log.info("report wedprJobTableList:{}", wedprJobTableList);
                        List<WedprJobTable> finalWedprJobTableList = wedprJobTableList;
                        reportWorker
                                .getThreadPool()
                                .execute(
                                        () -> {
                                            executeHandleReportJob(
                                                    message, finalWedprJobTableList, response);
                                        });
                    }
                });
    }

    private void executeHandleReportJob(
            IMessage message, List<WedprJobTable> wedprJobTableList, JobReportResponse response) {
        List<String> jobIdList = new ArrayList<>();
        byte[] responsePayload = null;
        try {
            for (WedprJobTable wedprJobTable : wedprJobTableList) {
                String jobId = wedprJobTable.getId();
                WedprJobTable queriedWedprJobTable = wedprJobTableService.getById(jobId);
                if (queriedWedprJobTable == null) {
                    wedprJobTableService.save(wedprJobTable);
                } else {
                    wedprJobTableService.updateById(wedprJobTable);
                }
                jobIdList.add(jobId);
            }
            response.setCode(Constant.WEDPR_SUCCESS);
            response.setMsg(Constant.WEDPR_SUCCESS_MSG);
            response.setJobIdList(jobIdList);
            log.info("report job ok, response:{}", response);
            log.info("report jobIdList size:{}", jobIdList.size());
            responsePayload = ObjectMapperFactory.getObjectMapper().writeValueAsBytes(response);
        } catch (Exception e) {
            log.error("handle error", e);
            response.setCode(Constant.WEDPR_FAILED);
            response.setMsg("handle error" + e.getMessage());
        }
        IMessage.IMessageHeader messageHeader = message.getHeader();
        weDPRTransport.asyncSendResponse(
                messageHeader.getSrcNode(),
                messageHeader.getTraceID(),
                responsePayload,
                0,
                new CommonErrorCallback("asyncSendResponseForJobSync"));
    }

    private void subscribeProjectTopic() {
        weDPRTransport.registerTopicHandler(
                TransportTopicEnum.PROJECT_REPORT.name(),
                new MessageDispatcherCallback() {
                    @Override
                    public void onMessage(IMessage message) {
                        log.info("receive project report");
                        byte[] payload = message.getPayload();
                        List<WedprProjectTable> wedprProjectTableList = null;
                        ProjectReportResponse response = new ProjectReportResponse();
                        try {
                            wedprProjectTableList =
                                    ObjectMapperFactory.getObjectMapper()
                                            .readValue(
                                                    payload,
                                                    new TypeReference<
                                                            List<WedprProjectTable>>() {});
                        } catch (IOException e) {
                            log.warn("parse message error", e);
                            response.setCode(Constant.WEDPR_FAILED);
                            response.setMsg("parse message error" + e.getMessage());
                        }
                        log.info("report wedprProjectTableList:{}", wedprProjectTableList);
                        List<WedprProjectTable> finalWedprProjectTableList = wedprProjectTableList;
                        log.info("report reportWorker:{}", reportWorker);
                        reportWorker
                                .getThreadPool()
                                .execute(
                                        () -> {
                                            executeHandleProject(
                                                    message, finalWedprProjectTableList, response);
                                        });
                    }
                });
    }

    private void executeHandleProject(
            IMessage message,
            List<WedprProjectTable> finalWedprProjectTableList,
            ProjectReportResponse response) {
        List<String> projectIdList = new ArrayList<>();
        byte[] responsePayload = null;
        try {
            log.info("report message:{}", message);
            log.info("report finalWedprProjectTableList:{}", finalWedprProjectTableList);
            for (WedprProjectTable wedprProjectTable : finalWedprProjectTableList) {
                String projectId = wedprProjectTable.getId();
                WedprProjectTable queriedWedprProjectTable =
                        wedprProjectTableService.getById(projectId);
                if (queriedWedprProjectTable == null) {
                    wedprProjectTableService.save(wedprProjectTable);
                } else {
                    wedprProjectTableService.updateById(wedprProjectTable);
                }
                projectIdList.add(projectId);
            }
            response.setCode(Constant.WEDPR_SUCCESS);
            response.setMsg(Constant.WEDPR_SUCCESS_MSG);
            response.setProjectIdList(projectIdList);
            log.info("report project ok, response:{}", response);
            log.info("report projectIdList size:{}", projectIdList.size());
            responsePayload = ObjectMapperFactory.getObjectMapper().writeValueAsBytes(response);
        } catch (Exception e) {
            log.error("handle error", e);
            response.setCode(Constant.WEDPR_FAILED);
            response.setMsg("handle error" + e.getMessage());
        }
        IMessage.IMessageHeader messageHeader = message.getHeader();
        weDPRTransport.asyncSendResponse(
                messageHeader.getSrcNode(),
                messageHeader.getTraceID(),
                responsePayload,
                0,
                new CommonErrorCallback("asyncSendResponseForProjectSync"));
    }
}
