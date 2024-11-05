package com.webank.wedpr.components.scheduler.client.response;

import com.webank.wedpr.components.scheduler.client.data.MpcContentData;
import lombok.Data;

@Data
public class TransferSQLResponse {
    private int errorCode;
    private String message;

    private MpcContentData data;
}
