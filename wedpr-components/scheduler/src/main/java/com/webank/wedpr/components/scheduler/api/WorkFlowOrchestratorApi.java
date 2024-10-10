package com.webank.wedpr.components.scheduler.api;

import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.scheduler.workflow.WorkFlow;

public interface WorkFlowOrchestratorApi {
    /**
     * build workflow by job
     *
     * @param jobDO
     * @return
     * @throws Exception
     */
    WorkFlow buildWorkFlow(JobDO jobDO) throws Exception;
}
