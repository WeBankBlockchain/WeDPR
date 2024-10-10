package com.webank.wedpr.components.scheduler.workflow.builder;

import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.scheduler.workflow.WorkFlow;
import com.webank.wedpr.components.scheduler.workflow.WorkFlowNode;

public interface WorkFlowBuilderDependencyHandler {
    void handleDependency(JobDO jobDO, WorkFlow workflow, WorkFlowNode workFlowNode)
            throws Exception;
}
