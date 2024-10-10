package com.webank.wedpr.components.scheduler.client.response;

import com.webank.wedpr.components.scheduler.client.data.MpcCodeData;
import lombok.Data;

@Data
public class TransferSQLResponse {
    private int errorCode;
    private String message;

    private MpcCodeData data;
}
