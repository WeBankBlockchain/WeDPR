package com.webank.wedpr.components.scheduler.workflow.builder;

import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.scheduler.executor.ExecuteResult;
import com.webank.wedpr.components.scheduler.executor.Executor;
import com.webank.wedpr.components.scheduler.workflow.WorkFlow;
import com.webank.wedpr.components.scheduler.workflow.WorkFlowNode;
import com.webank.wedpr.core.utils.ObjectMapperFactory;
import com.webank.wedpr.core.utils.WeDPRException;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobWorkFlowBuilderImpl implements JobWorkFlowBuilderApi {

    private static final Logger logger = LoggerFactory.getLogger(JobWorkFlowBuilderImpl.class);

    private final Executor executor;
    private final JobWorkFlowBuilderManager jobWorkflowBuilderManager;

    public JobWorkFlowBuilderImpl(
            Executor executor, JobWorkFlowBuilderManager jobWorkflowBuilderManager) {
        this.executor = executor;
        this.jobWorkflowBuilderManager = jobWorkflowBuilderManager;
    }

    @Override
    public WorkFlow createWorkFlow(JobDO jobDO) throws Exception {
        Object args = this.prepare(jobDO);
        if (args == null) {
            logger.error("executor prepare ret null, job: {}", jobDO);
            throw new WeDPRException("executor prepare ret null, jobId: " + jobDO.getId());
        }

        WorkFlow workflow = new WorkFlow(jobDO.getId());

        WorkFlowNode workflowNode = addWorkFlowNode(workflow, null, jobDO.getJobType(), args);
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
        Object args = this.prepare(jobDO);
        int index = upstream.getIndex();
        WorkFlowNode workflowNode =
                addWorkFlowNode(
                        workflow, Collections.singletonList(index), jobDO.getJobType(), args);

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
            WorkFlow workflow, List<Integer> upstreams, String jobType, Object args)
            throws Exception {
        // args
        String argsAsString = ObjectMapperFactory.getObjectMapper().writeValueAsString(args);
        // workflow build
        return workflow.addWorkFlowNode(upstreams, jobType, argsAsString);
    }

    @Override
    public Object prepare(JobDO jobDO) throws Exception {
        return executor.prepare(jobDO);
    }

    @Override
    public void execute(JobDO jobDO) throws Exception {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void kill(JobDO jobDO) throws Exception {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public ExecuteResult queryStatus(String jobID) throws Exception {
        throw new UnsupportedOperationException("Not supported");
    }
}
