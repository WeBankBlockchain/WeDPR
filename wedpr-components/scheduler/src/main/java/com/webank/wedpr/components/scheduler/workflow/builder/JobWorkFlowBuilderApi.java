package com.webank.wedpr.components.scheduler.workflow.builder;

import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.scheduler.executor.Executor;
import com.webank.wedpr.components.scheduler.workflow.WorkFlow;
import com.webank.wedpr.components.scheduler.workflow.WorkFlowNode;

public interface JobWorkFlowBuilderApi extends Executor {
    WorkFlow createWorkFlow(JobDO jobDO) throws Exception;

    void appendWorkFlowNode(JobDO jobDO, WorkFlow workflow, WorkFlowNode upstream) throws Exception;
}
