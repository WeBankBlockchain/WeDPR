package com.webank.wedpr.components.report.handler;

import com.webank.wedpr.components.meta.sys.config.dao.SysConfigDO;
import com.webank.wedpr.components.meta.sys.config.dao.SysConfigMapper;
import com.webank.wedpr.core.protocol.ReportStatusEnum;
import com.webank.wedpr.core.utils.Constant;
import com.webank.wedpr.core.utils.ObjectMapperFactory;
import com.webank.wedpr.sdk.jni.generated.Error;
import com.webank.wedpr.sdk.jni.generated.SendResponseHandler;
import com.webank.wedpr.sdk.jni.transport.IMessage;
import com.webank.wedpr.sdk.jni.transport.handlers.MessageCallback;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Created by caryliao on 2024/9/4 10:54 */
@Slf4j
public class SysConfigReportMessageHandler extends MessageCallback {
    private static final Logger logger =
            LoggerFactory.getLogger(SysConfigReportMessageHandler.class);

    private SysConfigMapper sysConfigMapper;

    public SysConfigReportMessageHandler(SysConfigMapper sysConfigMapper) {
        this.sysConfigMapper = sysConfigMapper;
    }

    @Override
    public void onMessage(Error error, IMessage msg, SendResponseHandler sendResponseHandler) {
        if (error != null && error.errorCode() != 0) {
            logger.warn(
                    "SysConfigReportMessageHandler error, code: {}, msg: {}",
                    error.errorCode(),
                    error.errorMessage());
            return;
        }
        byte[] payload = msg.getPayload();
        try {
            SysConfigReportResponse sysConfigReportResponse =
                    ObjectMapperFactory.getObjectMapper()
                            .readValue(payload, SysConfigReportResponse.class);
            if (Constant.WEDPR_SUCCESS == sysConfigReportResponse.getCode()) {
                // report ok ,then set report status to 1
                List<String> configKeyList = sysConfigReportResponse.getConfigKeyList();
                ArrayList<SysConfigDO> sysConfigDOList = new ArrayList<>();
                for (String configKey : configKeyList) {
                    SysConfigDO sysConfigDO = new SysConfigDO();
                    sysConfigDO.setConfigKey(configKey);
                    sysConfigDO.setReportStatus(ReportStatusEnum.DONE_REPORT.getReportStatus());
                    sysConfigDOList.add(sysConfigDO);
                }
                sysConfigMapper.batchUpdateSysConfig(sysConfigDOList);
            } else {
                log.warn("report sys config error:{}", sysConfigReportResponse);
            }
        } catch (IOException e) {
            log.warn("handle SysConfigReportResponse error", e);
        }
    }
}
