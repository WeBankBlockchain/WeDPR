package com.webank.wedpr.components.scheduler.executor.impl.remote;

import com.webank.wedpr.components.project.JobChecker;
import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.scheduler.api.WorkFlowOrchestratorApi;
import com.webank.wedpr.components.scheduler.client.SchedulerClient;
import com.webank.wedpr.components.scheduler.executor.ExecuteResult;
import com.webank.wedpr.components.scheduler.executor.Executor;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMetaBuilder;
import com.webank.wedpr.components.scheduler.workflow.WorkFlow;
import com.webank.wedpr.components.scheduler.workflow.WorkFlowOrchestrator;
import com.webank.wedpr.components.scheduler.workflow.builder.JobWorkFlowBuilderManager;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import com.webank.wedpr.core.utils.ObjectMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteSchedulerExecutor implements Executor {

    private static final Logger logger = LoggerFactory.getLogger(RemoteSchedulerExecutor.class);

    private final WorkFlowOrchestratorApi workflowOrchestrator;
    private final SchedulerClient schedulerClient;

    public RemoteSchedulerExecutor(
            SchedulerClient schedulerClient,
            JobChecker jobChecker,
            FileStorageInterface fileStorageInterface,
            FileMetaBuilder fileMetaBuilder) {

        this.schedulerClient = schedulerClient;
        // this.schedulerClient = new SchedulerClient();

        JobWorkFlowBuilderManager jobWorkflowBuilderManager =
                new JobWorkFlowBuilderManager(fileMetaBuilder, fileStorageInterface, jobChecker);

        this.workflowOrchestrator =
                new WorkFlowOrchestrator(jobWorkflowBuilderManager, fileMetaBuilder, jobChecker);

        jobWorkflowBuilderManager.initialize();
    }

    @Override
    public Object prepare(JobDO jobDO) throws Exception {
        return null;
    }

    @Override
    public void execute(JobDO jobDO) throws Exception {

        WorkFlow workflow = workflowOrchestrator.buildWorkFlow(jobDO);
        String workflowStr = ObjectMapperFactory.getObjectMapper().writeValueAsString(workflow);

        schedulerClient.postJob(jobDO.getId(), workflowStr);

        logger.info("post for run job successfully, jobId: {}", jobDO.getId());
    }

    @Override
    public void kill(JobDO jobDO) throws Exception {

        schedulerClient.killJob(jobDO.getId());

        logger.info("post for kill job successfully, jobId: {}", jobDO.getId());
    }

    @Override
    public ExecuteResult queryStatus(String jobId) throws Exception {
        ExecuteResult executeResult = schedulerClient.queryJob(jobId);
        if (executeResult.finished()) {
            logger.info(
                    "## job is finished by scheduler service, jobId: {}, result: {}",
                    jobId,
                    executeResult);
        } else if (logger.isDebugEnabled()) {
            logger.debug("## query status for job, jobId: {}, result: {}", jobId, executeResult);
        }

        return executeResult;
    }
}
