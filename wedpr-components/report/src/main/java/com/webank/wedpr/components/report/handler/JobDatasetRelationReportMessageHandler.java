package com.webank.wedpr.components.report.handler;

import com.webank.wedpr.common.protocol.ReportStatusEnum;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.components.project.dao.JobDatasetDO;
import com.webank.wedpr.components.project.dao.ProjectMapper;
import com.webank.wedpr.components.transport.message.JobDatasetReportResponse;
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
public class JobDatasetRelationReportMessageHandler extends MessageCallback {
    private static final Logger logger =
            LoggerFactory.getLogger(JobDatasetRelationReportMessageHandler.class);
    private ProjectMapper projectMapper;

    public JobDatasetRelationReportMessageHandler(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    @Override
    public void onMessage(Error error, IMessage msg, SendResponseHandler sendResponseHandler) {
        if (error != null && error.errorCode() != 0) {
            logger.warn(
                    "JobDatasetRelationReportMessageHandler exception, code: {}, msg: {}",
                    error.errorCode(),
                    error.errorMessage());
            return;
        }
        byte[] payload = msg.getPayload();
        try {
            JobDatasetReportResponse jobDatasetReportResponse =
                    ObjectMapperFactory.getObjectMapper()
                            .readValue(payload, JobDatasetReportResponse.class);
            if (Constant.WEDPR_SUCCESS == jobDatasetReportResponse.getCode()) {
                // report ok ,then set report status to 1
                List<String> jobIdList = jobDatasetReportResponse.getJobIdList();
                ArrayList<JobDatasetDO> jobDatasetDOList = new ArrayList<>();
                for (String jobId : jobIdList) {
                    JobDatasetDO jobDatasetDO = new JobDatasetDO();
                    jobDatasetDO.setJobId(jobId);
                    jobDatasetDO.setReportStatus(ReportStatusEnum.DONE_REPORT.getReportStatus());
                    jobDatasetDOList.add(jobDatasetDO);
                }
                projectMapper.batchUpdateJobDatasetInfo(jobDatasetDOList);
            } else {
                log.warn("report job dataset relation error:{}", jobDatasetReportResponse);
            }
        } catch (IOException e) {
            log.warn("handle JobDatasetReportResponse error", e);
        }
    }
}
