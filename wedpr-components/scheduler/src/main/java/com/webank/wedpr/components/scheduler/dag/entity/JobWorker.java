package com.webank.wedpr.components.scheduler.dag.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.components.scheduler.workflow.WorkFlowUpstream;
import java.util.List;
import lombok.Data;

@Data
public class JobWorker {
    private String workerId;
    private String jobId;
    private String type;
    private String status;
    private String args;
    private String upstreams;
    // private String inputsStatement;
    // private String outputs;
    private String createTime;
    private String updateTime;
    // Note: execResult is updated by the worker
    private String execResult;

    public List<WorkFlowUpstream> toUpstreams() throws JsonProcessingException {
        return ObjectMapperFactory.getObjectMapper()
                .readValue(upstreams, new TypeReference<List<WorkFlowUpstream>>() {});
    }
}
