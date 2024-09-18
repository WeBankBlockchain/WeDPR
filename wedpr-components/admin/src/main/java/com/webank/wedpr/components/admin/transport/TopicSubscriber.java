package com.webank.wedpr.components.admin.transport;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.webank.wedpr.components.admin.entity.WedprJobDatasetRelation;
import com.webank.wedpr.components.admin.entity.WedprJobTable;
import com.webank.wedpr.components.admin.entity.WedprProjectTable;
import com.webank.wedpr.components.admin.service.WedprJobDatasetRelationService;
import com.webank.wedpr.components.admin.service.WedprJobTableService;
import com.webank.wedpr.components.admin.service.WedprProjectTableService;
import com.webank.wedpr.components.meta.sys.config.dao.SysConfigDO;
import com.webank.wedpr.components.meta.sys.config.service.SysConfigService;
import com.webank.wedpr.core.protocol.TransportTopicEnum;
import com.webank.wedpr.core.utils.ObjectMapperFactory;
import com.webank.wedpr.sdk.jni.transport.IMessage;
import com.webank.wedpr.sdk.jni.transport.WeDPRTransport;
import com.webank.wedpr.sdk.jni.transport.handlers.MessageDispatcherCallback;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

/** Created by caryliao on 2024/9/4 15:03 */
@Service
@Slf4j
public class TopicSubscriber implements CommandLineRunner {
    @Autowired private WeDPRTransport weDPRTransport;

    @Autowired private WedprProjectTableService wedprProjectTableService;
    @Autowired private WedprJobTableService wedprJobTableService;
    @Autowired private WedprJobDatasetRelationService wedprJobDatasetRelationService;
    @Autowired private SysConfigService sysConfigService;

    @Override
    public void run(String... args) throws Exception {
        try {
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
                        byte[] payload = message.getPayload();
                        List<SysConfigDO> sysConfigDOList = null;
                        try {
                            sysConfigDOList =
                                    (List<SysConfigDO>)
                                            ObjectMapperFactory.getObjectMapper()
                                                    .readValue(payload, List.class);
                        } catch (IOException e) {
                            log.warn("parse message error", e);
                        }
                        for (SysConfigDO sysConfigDO : sysConfigDOList) {
                            String configKey = sysConfigDO.getConfigKey();
                            SysConfigDO queriedSysConfigDO = sysConfigService.getById(configKey);
                            if (queriedSysConfigDO == null) {
                                sysConfigService.save(sysConfigDO);
                            } else {
                                sysConfigService.updateById(sysConfigDO);
                            }
                        }
                        // TODO send response message
                        //            transport.asyncSendMessage()
                    }
                });
    }

    private void subscribeJobDatasetRelationTopic() {
        weDPRTransport.registerTopicHandler(
                TransportTopicEnum.JOB_DATASET_REPORT.name(),
                new MessageDispatcherCallback() {
                    @Override
                    public void onMessage(IMessage message) {
                        byte[] payload = message.getPayload();
                        List<WedprJobDatasetRelation> wedprJobDatasetRelationList = null;
                        try {
                            wedprJobDatasetRelationList =
                                    (List<WedprJobDatasetRelation>)
                                            ObjectMapperFactory.getObjectMapper()
                                                    .readValue(payload, List.class);
                        } catch (IOException e) {
                            log.warn("parse message error", e);
                        }
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
                        }
                        // TODO send response message
                        //            transport.asyncSendMessage()
                    }
                });
    }

    private void subscribeJobTopic() {
        weDPRTransport.registerTopicHandler(
                TransportTopicEnum.JOB_REPORT.name(),
                new MessageDispatcherCallback() {
                    @Override
                    public void onMessage(IMessage message) {
                        byte[] payload = message.getPayload();
                        List<WedprJobTable> wedprJobTableList = null;
                        try {
                            wedprJobTableList =
                                    (List<WedprJobTable>)
                                            ObjectMapperFactory.getObjectMapper()
                                                    .readValue(payload, List.class);
                        } catch (IOException e) {
                            log.warn("parse message error", e);
                        }
                        for (WedprJobTable wedprJobTable : wedprJobTableList) {
                            String id = wedprJobTable.getId();
                            WedprJobTable queriedWedprJobTable = wedprJobTableService.getById(id);
                            if (queriedWedprJobTable == null) {
                                wedprJobTableService.save(wedprJobTable);
                            } else {
                                wedprJobTableService.updateById(wedprJobTable);
                            }
                        }
                        // TODO send response message
                        //            transport.asyncSendMessage()
                    }
                });
    }

    private void subscribeProjectTopic() {
        weDPRTransport.registerTopicHandler(
                TransportTopicEnum.PROJECT_REPORT.name(),
                new MessageDispatcherCallback() {
                    @Override
                    public void onMessage(IMessage message) {
                        byte[] payload = message.getPayload();
                        List<WedprProjectTable> wedprProjectTableList = null;
                        try {
                            wedprProjectTableList =
                                    (List<WedprProjectTable>)
                                            ObjectMapperFactory.getObjectMapper()
                                                    .readValue(payload, List.class);
                        } catch (IOException e) {
                            log.warn("parse message error", e);
                        }
                        for (WedprProjectTable wedprProjectTable : wedprProjectTableList) {
                            String id = wedprProjectTable.getId();
                            WedprProjectTable queriedWedprProjectTable =
                                    wedprProjectTableService.getById(id);
                            if (queriedWedprProjectTable == null) {
                                wedprProjectTableService.save(wedprProjectTable);
                            } else {
                                wedprProjectTableService.updateById(wedprProjectTable);
                            }
                        }
                        // TODO send response message
                        //            transport.asyncSendMessage()
                    }
                });
    }
}
