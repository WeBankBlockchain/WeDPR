package com.webank.wedpr.components.scheduler.executor.impl.mpc.request;

import lombok.Data;

@Data
public class MPCJobRequest {
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
    private String gatewayEngineEndpoint;
}

/*
job_info = {
        "jobId": job_id,
        "mpcNodeUseGateway": False,
        "receiverNodeIp": "",
        "mpcNodeDirectPort": self.components.config_data["MPC_NODE_DIRECT_PORT"],
        "participantCount": len(self.job_context.participant_id_list),
            "selfIndex": self.job_context.my_index,
        "isMalicious": self.components.config_data["IS_MALICIOUS"],
        "bitLength": bit_length,
        "inputFileName": "{}-P{}-0".format(JobContext.MPC_PREPARE_FILE, self.job_context.my_index),
            "outputFileName": JobContext.MPC_OUTPUT_FILE
        }*/
