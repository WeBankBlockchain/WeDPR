package com.webank.wedpr.components.scheduler.dag.api;

import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.scheduler.workflow.WorkFlow;

public interface WorkFlowScheduler {
    void schedule(String jobId, WorkFlow workFlow) throws WeDPRException;
}
