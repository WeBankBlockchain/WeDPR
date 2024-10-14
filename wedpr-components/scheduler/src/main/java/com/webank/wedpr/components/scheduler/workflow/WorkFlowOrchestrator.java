package com.webank.wedpr.components.scheduler.workflow;

import com.webank.wedpr.common.protocol.JobType;
import com.webank.wedpr.components.project.JobChecker;
import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.scheduler.api.WorkFlowOrchestratorApi;
import com.webank.wedpr.components.scheduler.executor.impl.ml.model.ModelJobParam;
import com.webank.wedpr.components.scheduler.executor.impl.ml.request.PreprocessingRequest;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMetaBuilder;
import com.webank.wedpr.components.scheduler.executor.impl.mpc.MPCJobParam;
import com.webank.wedpr.components.scheduler.workflow.builder.JobWorkFlowBuilderManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkFlowOrchestrator implements WorkFlowOrchestratorApi {

    private static final Logger logger = LoggerFactory.getLogger(WorkFlowOrchestrator.class);

    private JobWorkFlowBuilderManager jobWorkflowBuilderManager = null;
    private FileMetaBuilder fileMetaBuilder = null;
    private JobChecker jobChecker = null;

    public JobChecker getJobChecker() {
        return jobChecker;
    }

    public void setJobChecker(JobChecker jobChecker) {
        this.jobChecker = jobChecker;
    }

    public WorkFlowOrchestrator(
            JobWorkFlowBuilderManager jobWorkflowBuilderManager,
            FileMetaBuilder fileMetaBuilder,
            JobChecker jobChecker) {
        this.jobWorkflowBuilderManager = jobWorkflowBuilderManager;
        this.fileMetaBuilder = fileMetaBuilder;
        this.jobChecker = jobChecker;
    }

    /**
     * build workflow by job
     *
     * @param jobDO
     * @return
     * @throws Exception
     */
    @Override
    public WorkFlow buildWorkFlow(JobDO jobDO) throws Exception {
        String jobId = jobDO.getId();
        String jobType = jobDO.getJobType();

        logger.info("build workflow, jobId: {}, jobType: {}", jobId, jobType);

        WorkFlow workflow = null;

        jobDO.setOriginalJobType(jobDO.getType());
        if (JobType.isPSIJob(jobType)) {
            workflow = buildPSIWorkFlow(jobDO);
        } else if (JobType.isXGBJob(jobType)) {
            workflow = buildXGBWorkFlow(jobDO, workflow);
        } else if (JobType.isMPCJob(jobType)) {
            workflow = buildMPCWorkFlow(jobDO, workflow);
        } else {
            throw new UnsupportedOperationException("Unsupported job type: " + jobType);
        }

        return workflow;
    }

    // PSI
    public WorkFlow buildPSIWorkFlow(JobDO jobDO) throws Exception {
        String jobType = jobDO.getJobType();
        //        logger.info("build PSI workflow, jobId: {}", jobDO.getId());
        return jobWorkflowBuilderManager.getJobWorkFlowBuilder(jobType).createWorkFlow(jobDO);
    }

    // ML
    public WorkFlow buildXGBWorkFlow(JobDO jobDO, WorkFlow workflow) throws Exception {

        ModelJobParam modelJobParam = (ModelJobParam) jobChecker.checkAndParseParam(jobDO);
        jobDO.setJobParam(modelJobParam);

        logger.info(
                "build xgb workflow, usePSI: {}, jobId: {}", modelJobParam.usePSI(), jobDO.getId());

        if (modelJobParam.usePSI()) {
            // ml-psi
            jobDO.setJobType(JobType.ML_PSI.getType());
        } else {
            // ml-preprocessing
            PreprocessingRequest preprocessingRequest =
                    modelJobParam.toPreprocessingRequest(fileMetaBuilder);
            jobDO.setJobRequest(preprocessingRequest);
            jobDO.setJobType(JobType.MLPreprocessing.getType());
        }

        return jobWorkflowBuilderManager
                .getJobWorkFlowBuilder(jobDO.getJobType())
                .createWorkFlow(jobDO);
    }

    // MPC
    public WorkFlow buildMPCWorkFlow(JobDO jobDO, WorkFlow workFlow) throws Exception {
        MPCJobParam mpcJobParam = (MPCJobParam) jobChecker.checkAndParseParam(jobDO);
        jobDO.setJobParam(mpcJobParam);

        if (mpcJobParam.checkNeedRunPSI()) {
            // mpc-psi
            jobDO.setJobType(JobType.MPC_PSI.getType());
        }

        return jobWorkflowBuilderManager
                .getJobWorkFlowBuilder(jobDO.getJobType())
                .createWorkFlow(jobDO);
    }
}
