package com.webank.wedpr.components.report.handler;

import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.project.dao.ProjectMapper;
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
public class JobReportMessageHandler extends MessageCallback {
    private static final Logger logger = LoggerFactory.getLogger(JobReportMessageHandler.class);
    private ProjectMapper projectMapper;

    public JobReportMessageHandler(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    @Override
    public void onMessage(Error error, IMessage msg, SendResponseHandler sendResponseHandler) {
        if (error != null && error.errorCode() != 0) {
            logger.warn(
                    "JobReportMessageHandler exception, code: {}, msg: {}",
                    error.errorCode(),
                    error.errorMessage());
            return;
        }
        byte[] payload = msg.getPayload();
        try {
            JobReportResponse jobReportResponse =
                    ObjectMapperFactory.getObjectMapper()
                            .readValue(payload, JobReportResponse.class);
            if (Constant.WEDPR_SUCCESS == jobReportResponse.getCode()) {
                // report ok ,then set report status to 1
                List<String> jobIdList = jobReportResponse.getJobIdList();
                ArrayList<JobDO> jobDOList = new ArrayList<>();
                for (String jobId : jobIdList) {
                    JobDO jobDO = new JobDO();
                    jobDO.setId(jobId);
                    jobDO.setReportStatus(ReportStatusEnum.DONE_REPORT.getReportStatus());
                    jobDOList.add(jobDO);
                }
                projectMapper.batchUpdateJobInfo(jobDOList);
            } else {
                log.warn("report job error:{}", jobReportResponse);
            }
        } catch (IOException e) {
            log.warn("handle JobReportResponse error", e);
        }
    }
}
