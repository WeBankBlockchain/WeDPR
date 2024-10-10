package com.webank.wedpr.components.scheduler.client.response;

import com.webank.wedpr.components.scheduler.client.data.JobData;
import lombok.Data;

@Data
public class QueryJobResponse {
    private int errorCode;
    private String message;

    private JobData data;
};
