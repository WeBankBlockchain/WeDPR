package com.webank.wedpr.components.scheduler.workflow.builder;

import com.webank.wedpr.common.protocol.JobType;
import com.webank.wedpr.components.project.JobChecker;
import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.scheduler.executor.impl.ml.MLExecutor;
import com.webank.wedpr.components.scheduler.executor.impl.ml.model.ModelJobParam;
import com.webank.wedpr.components.scheduler.executor.impl.ml.request.FeatureEngineeringRequest;
import com.webank.wedpr.components.scheduler.executor.impl.ml.request.ModelJobRequest;
import com.webank.wedpr.components.scheduler.executor.impl.ml.request.PreprocessingRequest;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMetaBuilder;
import com.webank.wedpr.components.scheduler.executor.impl.mpc.MPCJobParam;
import com.webank.wedpr.components.scheduler.executor.impl.psi.MLPSIExecutor;
import com.webank.wedpr.components.scheduler.executor.impl.psi.MPCExecutor;
import com.webank.wedpr.components.scheduler.executor.impl.psi.MPCPSIExecutor;
import com.webank.wedpr.components.scheduler.executor.impl.psi.PSIExecutor;
import com.webank.wedpr.components.scheduler.workflow.WorkFlow;
import com.webank.wedpr.components.scheduler.workflow.WorkFlowNode;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobWorkFlowBuilderManager {

    private static final Logger logger = LoggerFactory.getLogger(JobWorkFlowBuilderManager.class);

    private final FileMetaBuilder fileMetaBuilder;
    private final FileStorageInterface storage;
    private final JobChecker jobChecker;

    public JobChecker getJobChecker() {
        return jobChecker;
    }

    protected Map<String, JobWorkFlowBuilderApi> jobWorkFlowBuilderMap = new ConcurrentHashMap<>();

    protected Map<String, List<WorkFlowBuilderDependencyHandler>> jobWorkFlowDependencyHandlerMap =
            new ConcurrentHashMap<>();

    public JobWorkFlowBuilderManager(
            FileMetaBuilder fileMetaBuilder, FileStorageInterface storage, JobChecker jobChecker) {
        this.fileMetaBuilder = fileMetaBuilder;
        this.storage = storage;
        this.jobChecker = jobChecker;
    }

    public void initialize() {
        initializeJobWorkFlowBuilderManager();
        initializeJobWorkFlowDependencyHandler();
    }

    public void initializeJobWorkFlowBuilderManager() {
        logger.info("register PSI workflow builder success");
        registerJobWorkFlowBuilder(
                JobType.PSI.getType(),
                new JobWorkFlowBuilderImpl(
                        new PSIExecutor(storage, fileMetaBuilder, jobChecker), this));

        logger.info("register ML PSI workflow builder success");
        registerJobWorkFlowBuilder(
                JobType.ML_PSI.getType(),
                new JobWorkFlowBuilderImpl(new MLPSIExecutor(storage, fileMetaBuilder), this));

        logger.info("register MPC PSI workflow builder success");
        registerJobWorkFlowBuilder(
                JobType.MPC_PSI.getType(),
                new JobWorkFlowBuilderImpl(new MPCPSIExecutor(storage, fileMetaBuilder), this));

        logger.info("register MPC workflow builder success");
        registerJobWorkFlowBuilder(
                JobType.MPC.getType(), new JobWorkFlowBuilderImpl(new MPCExecutor(), this));

        logger.info("register ML workflow builder success");
        registerJobWorkFlowBuilder(
                JobType.MLPreprocessing.getType(),
                new JobWorkFlowBuilderImpl(new MLExecutor(), this));
        registerJobWorkFlowBuilder(
                JobType.FeatureEngineer.getType(),
                new JobWorkFlowBuilderImpl(new MLExecutor(), this));
        registerJobWorkFlowBuilder(
                JobType.XGB_TRAIN.getType(), new JobWorkFlowBuilderImpl(new MLExecutor(), this));
        registerJobWorkFlowBuilder(
                JobType.XGB_PREDICT.getType(), new JobWorkFlowBuilderImpl(new MLExecutor(), this));
        registerJobWorkFlowBuilder(
                JobType.LR_TRAIN.getType(), new JobWorkFlowBuilderImpl(new MLExecutor(), this));
        registerJobWorkFlowBuilder(
                JobType.LR_PREDICT.getType(), new JobWorkFlowBuilderImpl(new MLExecutor(), this));

        logger.info("register job workflow builder end");
    }

    public void initializeJobWorkFlowDependencyHandler() {
        registerJobWorkFlowDependencyHandler(
                JobType.ML_PSI.getType(),
                (jobDO, workflow, upstream) -> {
                    // the next work is preprocessing
                    ModelJobParam modelJobParam = (ModelJobParam) jobDO.getJobParam();
                    PreprocessingRequest preprocessingRequest =
                            modelJobParam.toPreprocessingRequest(fileMetaBuilder);
                    jobDO.setJobRequest(preprocessingRequest);
                    jobDO.setJobType(JobType.MLPreprocessing.getType());

                    JobWorkFlowBuilderImpl jobWorkFlowBuilder =
                            new JobWorkFlowBuilderImpl(
                                    getJobWorkFlowBuilder(jobDO.getJobType()), this);
                    jobWorkFlowBuilder.appendWorkFlowNode(jobDO, workflow, upstream);
                });

        registerJobWorkFlowDependencyHandler(
                JobType.MPC_PSI.getType(),
                (jobDO, workflow, upstream) -> {
                    MPCJobParam mpcJobParam = (MPCJobParam) jobDO.getJobParam();
                    mpcJobParam.toMPCJobRequest();
                    /*
                    PreprocessingRequest preprocessingRequest =
                            mpcJobParam.toPreprocessingRequest(fileMetaBuilder);
                    jobDO.setJobRequest(preprocessingRequest);
                    jobDO.setJobType(JobType.MPC.getType());
                    */

                    JobWorkFlowBuilderImpl jobWorkFlowBuilder =
                            new JobWorkFlowBuilderImpl(
                                    getJobWorkFlowBuilder(jobDO.getJobType()), this);
                    jobWorkFlowBuilder.appendWorkFlowNode(jobDO, workflow, upstream);
                });

        registerJobWorkFlowDependencyHandler(
                JobType.MLPreprocessing.getType(),
                (jobDO, workflow, upstream) -> {
                    if (jobDO.getOriginalJobType() == null) {
                        return;
                    }
                    // the next work is feature-engineer or multi-party-ml-job
                    ModelJobParam modelJobParam = (ModelJobParam) jobDO.getJobParam();
                    jobDO.setJobType(JobType.FeatureEngineer.getType());
                    // try to execute FeatureEngineer job
                    if (executeFeatureEngineerJob(jobDO, modelJobParam, workflow, upstream)) {
                        return;
                    }

                    // execute xgb request
                    jobDO.setType(jobDO.getOriginalJobType());
                    if (executeMultiPartyMlJob(jobDO, modelJobParam, workflow, upstream)) {
                        return;
                    }
                });

        registerJobWorkFlowDependencyHandler(
                JobType.FeatureEngineer.getType(),
                (jobDO, workflow, upstream) -> {
                    jobDO.setType(jobDO.getOriginalJobType());
                    ModelJobParam modelJobParam = (ModelJobParam) jobDO.getJobParam();
                    // the next work is multi-party-ml-job
                    executeMultiPartyMlJob(jobDO, modelJobParam, workflow, upstream);
                });
    }

    private boolean executeFeatureEngineerJob(
            JobDO jobDO, ModelJobParam modelJobParam, WorkFlow workflow, WorkFlowNode upstream)
            throws Exception {
        // try to execute FeatureEngineer job
        FeatureEngineeringRequest featureEngineeringRequest =
                modelJobParam.toFeatureEngineerRequest();
        if (featureEngineeringRequest == null) {
            return false;
        }

        jobDO.setJobRequest(featureEngineeringRequest);

        JobWorkFlowBuilderImpl jobWorkFlowBuilder =
                new JobWorkFlowBuilderImpl(getJobWorkFlowBuilder(jobDO.getJobType()), this);
        jobWorkFlowBuilder.appendWorkFlowNode(jobDO, workflow, upstream);
        return true;
    }

    private boolean executeMultiPartyMlJob(
            JobDO jobDO, ModelJobParam modelJobParam, WorkFlow workflow, WorkFlowNode upstream)
            throws Exception {
        // execute xgb request
        ModelJobRequest xgbJobRequest = modelJobParam.toMultiPartyMlJobRequest();
        if (xgbJobRequest == null) {
            return false;
        }

        jobDO.setJobRequest(xgbJobRequest);

        JobWorkFlowBuilderImpl jobWorkFlowBuilder =
                new JobWorkFlowBuilderImpl(getJobWorkFlowBuilder(jobDO.getJobType()), this);
        jobWorkFlowBuilder.appendWorkFlowNode(jobDO, workflow, upstream);
        return true;
    }

    public void registerJobWorkFlowBuilder(
            String jobType, JobWorkFlowBuilderApi jobWorkFlowBuilder) {
        jobWorkFlowBuilderMap.put(jobType, jobWorkFlowBuilder);
        logger.info("register builder : {}", jobType);
    }

    public JobWorkFlowBuilderApi getJobWorkFlowBuilder(String jobType) {
        JobWorkFlowBuilderApi jobWorkFlowBuilder = jobWorkFlowBuilderMap.get(jobType);
        if (jobWorkFlowBuilder == null) {
            logger.error("Unsupported job workflow type, jobType: {}", jobType);
            throw new UnsupportedOperationException(
                    "Unsupported job workflow type, jobType: " + jobType);
        }

        return jobWorkFlowBuilder;
    }

    public void registerJobWorkFlowDependencyHandler(
            String jobType, WorkFlowBuilderDependencyHandler workFlowBuilderDependencyHandler) {
        List<WorkFlowBuilderDependencyHandler> workFlowBuilderDependencyHandlers =
                jobWorkFlowDependencyHandlerMap.computeIfAbsent(jobType, k -> new ArrayList<>());

        workFlowBuilderDependencyHandlers.add(workFlowBuilderDependencyHandler);
    }

    public List<WorkFlowBuilderDependencyHandler> getHandler(String jobType) {
        return jobWorkFlowDependencyHandlerMap.get(jobType);
    }
}
