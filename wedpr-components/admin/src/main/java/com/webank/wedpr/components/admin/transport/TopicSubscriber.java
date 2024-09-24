package com.webank.wedpr.components.admin.transport;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
import com.webank.wedpr.core.protocol.TransportComponentEnum;
import com.webank.wedpr.core.protocol.TransportTopicEnum;
import com.webank.wedpr.core.utils.Constant;
import com.webank.wedpr.core.utils.ObjectMapperFactory;
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
    @Autowired private WeDPRTransport weDPRTransport;

    @Autowired private WedprProjectTableService wedprProjectTableService;
    @Autowired private WedprJobTableService wedprJobTableService;
    @Autowired private WedprJobDatasetRelationService wedprJobDatasetRelationService;
    @Autowired private SysConfigMapper sysConfigMapper;

    @Override
    public void run(String... args) throws Exception {
        try {
            weDPRTransport.registerComponent(TransportComponentEnum.REPORT.name());
            logger.info(
                    "TopicSubscriber: registerComponent: {}", TransportComponentEnum.REPORT.name());
            subscribeProjectTopic();
            subscribeJobTopic();
            subscribeJobDatasetRelationTopic();
            subscribeSysConfigTopic();
        } catch (Exception e) {
            log.warn("subscribe topic error", e);
        }
    }

    private void subscribeSysConfigTopic() {
        weDPRTransport.registerTopicHandler(
                TransportTopicEnum.SYS_CONFIG_REPORT.name(),
                new MessageDispatcherCallback() {
                    @Override
                    public void onMessage(IMessage message) {
                        log.info("receive sys config report");
                        byte[] payload = message.getPayload();
                        List<SysConfigDO> sysConfigDOList = null;
                        SysConfigReportResponse response = new SysConfigReportResponse();
                        try {
                            sysConfigDOList =
                                    ObjectMapperFactory.getObjectMapper()
                                            .readValue(
                                                    payload,
                                                    new TypeReference<List<SysConfigDO>>() {});
                        } catch (IOException e) {
                            log.warn("parse message error", e);
                            response.setCode(Constant.WEDPR_FAILED);
                            response.setMsg("parse message error" + e.getMessage());
                        }
                        log.debug("report wedprSysConfigDOList:{}", sysConfigDOList);
                        List<String> configKeyList = new ArrayList<>();
                        byte[] responsePayload = null;
                        try {
                            for (SysConfigDO sysConfigDO : sysConfigDOList) {
                                String configKey = sysConfigDO.getConfigKey();
                                SysConfigDO queriedSysConfigDO =
                                        sysConfigMapper.queryConfig(configKey);
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
                            responsePayload =
                                    ObjectMapperFactory.getObjectMapper()
                                            .writeValueAsBytes(response);
                        } catch (JsonProcessingException e) {
                            log.error("handle error", e);
                            response.setCode(Constant.WEDPR_FAILED);
                            response.setMsg("handle error" + e.getMessage());
                        }
                        logger.debug(
                                "asyncSendSysConfigResponse, dstNode: {}, traceID: {}, data: {}",
                                new String(message.getHeader().getSrcNode()),
                                message.getHeader().getTraceID(),
                                response.toString());
                        weDPRTransport.asyncSendResponse(
                                message.getHeader().getSrcNode(),
                                message.getHeader().getTraceID(),
                                responsePayload,
                                0,
                                new CommonErrorCallback("asyncSendSysConfigResponse"));
                    }
                });
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
                        log.debug(
                                "report wedprJobDatasetRelationList:{}",
                                wedprJobDatasetRelationList);
                        List<String> jobIdList = new ArrayList<>();
                        byte[] responsePayload = null;
                        try {
                            for (WedprJobDatasetRelation wedprJobDatasetRelation :
                                    wedprJobDatasetRelationList) {
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
                            response.setCode(Constant.WEDPR_FAILED);
                            response.setMsg(Constant.WEDPR_SUCCESS_MSG);
                            response.setJobIdList(jobIdList);
                            responsePayload =
                                    ObjectMapperFactory.getObjectMapper()
                                            .writeValueAsBytes(response);
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
                });
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
                        log.debug("report wedprJobTableList:{}", wedprJobTableList);
                        List<String> jobIdList = new ArrayList<>();
                        byte[] responsePayload = null;
                        try {
                            for (WedprJobTable wedprJobTable : wedprJobTableList) {
                                String jobId = wedprJobTable.getId();
                                WedprJobTable queriedWedprJobTable =
                                        wedprJobTableService.getById(jobId);
                                if (queriedWedprJobTable == null) {
                                    wedprJobTableService.save(wedprJobTable);
                                } else {
                                    wedprJobTableService.updateById(wedprJobTable);
                                }
                                jobIdList.add(jobId);
                            }
                            response.setCode(Constant.WEDPR_FAILED);
                            response.setMsg(Constant.WEDPR_SUCCESS_MSG);
                            response.setJobIdList(jobIdList);
                            responsePayload =
                                    ObjectMapperFactory.getObjectMapper()
                                            .writeValueAsBytes(response);
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
                });
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
                        log.debug("report wedprProjectTableList:{}", wedprProjectTableList);
                        List<String> projectIdList = new ArrayList<>();
                        byte[] responsePayload = null;
                        try {
                            for (WedprProjectTable wedprProjectTable : wedprProjectTableList) {
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
                            response.setCode(Constant.WEDPR_FAILED);
                            response.setMsg(Constant.WEDPR_SUCCESS_MSG);
                            response.setProjectIdList(projectIdList);
                            responsePayload =
                                    ObjectMapperFactory.getObjectMapper()
                                            .writeValueAsBytes(response);
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
                });
    }
}
