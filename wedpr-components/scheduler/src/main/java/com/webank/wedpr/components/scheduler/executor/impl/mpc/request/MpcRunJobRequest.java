package com.webank.wedpr.components.scheduler.executor.impl.mpc.request;

import lombok.Data;

@Data
public class MpcRunJobRequest {
    private String jobId;
    private boolean mpcNodeUseGateway = false;
    private String receiverNodeIp = "";
    private int mpcNodeDirectPort;
    private int participantCount;
    private int selfIndex;
    private boolean isMalicious;
    private int bitLength;
    private String inputFileName;
    private String outputFileName;
}
