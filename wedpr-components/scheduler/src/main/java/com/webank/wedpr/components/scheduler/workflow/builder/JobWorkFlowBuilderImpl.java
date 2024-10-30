package com.webank.wedpr.components.scheduler.workflow.builder;

import com.webank.wedpr.common.protocol.WorkerNodeType;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.scheduler.executor.hook.ExecutorHook;
import com.webank.wedpr.components.scheduler.workflow.WorkFlow;
import com.webank.wedpr.components.scheduler.workflow.WorkFlowNode;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobWorkFlowBuilderImpl implements JobWorkFlowBuilderApi {

    private static final Logger logger = LoggerFactory.getLogger(JobWorkFlowBuilderImpl.class);

    private final ExecutorHook executorHook;
    private final JobWorkFlowBuilderManager jobWorkflowBuilderManager;

    public JobWorkFlowBuilderImpl(
            ExecutorHook executorHook, JobWorkFlowBuilderManager jobWorkflowBuilderManager) {
        this.executorHook = executorHook;
        this.jobWorkflowBuilderManager = jobWorkflowBuilderManager;
    }

    @Override
    public ExecutorHook getExecutorHook() {
        return this.executorHook;
    }

    @Override
    public WorkFlow createWorkFlow(JobDO jobDO) throws Exception {
        Object args = this.executorHook.prepare(jobDO);
        if (args == null) {
            logger.error("executor prepare ret null, job: {}", jobDO);
            throw new WeDPRException("executor prepare ret null, jobId: " + jobDO.getId());
        }

        WorkFlow workflow = new WorkFlow(jobDO.getId());

        WorkFlowNode workflowNode =
                addWorkFlowNode(workflow, null, jobDO.getType().getWorkerNodeType(), args);
        List<WorkFlowBuilderDependencyHandler> depsHandlers =
                jobWorkflowBuilderManager.getHandler(jobDO.getJobType());
        if (depsHandlers != null) {
            for (WorkFlowBuilderDependencyHandler depsHandler : depsHandlers) {
                depsHandler.handleDependency(jobDO, workflow, workflowNode);
            }
        }

        return workflow;
    }

    @Override
    public void appendWorkFlowNode(JobDO jobDO, WorkFlow workflow, WorkFlowNode upstream)
            throws Exception {
        Object args = this.executorHook.prepare(jobDO);
        int index = upstream.getIndex();
        WorkFlowNode workflowNode =
                addWorkFlowNode(
                        workflow,
                        Collections.singletonList(index),
                        jobDO.getType().getWorkerNodeType(),
                        args);

        List<WorkFlowBuilderDependencyHandler> handlers =
                jobWorkflowBuilderManager.getHandler(jobDO.getJobType());
        if (handlers == null) {
            return;
        }

        for (WorkFlowBuilderDependencyHandler handler : handlers) {
            handler.handleDependency(jobDO, workflow, workflowNode);
        }
    }

    private WorkFlowNode addWorkFlowNode(
            WorkFlow workflow, List<Integer> upstreams, WorkerNodeType workerNodeType, Object args)
            throws Exception {
        // args
        // String argsAsString = ObjectMapperFactory.getObjectMapper().writeValueAsString(args);
        // workflow build
        return workflow.addWorkFlowNode(upstreams, workerNodeType.getType(), args);
    }
}
