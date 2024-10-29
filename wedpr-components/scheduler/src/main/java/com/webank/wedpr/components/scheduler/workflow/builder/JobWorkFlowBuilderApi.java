package com.webank.wedpr.components.scheduler.workflow.builder;

import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.scheduler.executor.hook.ExecutorHook;
import com.webank.wedpr.components.scheduler.workflow.WorkFlow;
import com.webank.wedpr.components.scheduler.workflow.WorkFlowNode;

public interface JobWorkFlowBuilderApi {
    WorkFlow createWorkFlow(JobDO jobDO) throws Exception;

    void appendWorkFlowNode(JobDO jobDO, WorkFlow workflow, WorkFlowNode upstream) throws Exception;

    public abstract ExecutorHook getExecutorHook();
}
