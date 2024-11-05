package com.webank.wedpr.components.scheduler.executor.hook;

import com.webank.wedpr.common.config.WeDPRCommonConfig;
import com.webank.wedpr.common.protocol.JobType;
import com.webank.wedpr.common.utils.Common;
import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.scheduler.executor.impl.ExecutorConfig;
import com.webank.wedpr.components.scheduler.executor.impl.model.DatasetInfo;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMeta;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMetaBuilder;
import com.webank.wedpr.components.scheduler.executor.impl.mpc.MPCExecutorConfig;
import com.webank.wedpr.components.scheduler.executor.impl.mpc.MPCJobParam;
import com.webank.wedpr.components.scheduler.executor.impl.mpc.request.MpcRunJobRequest;
import com.webank.wedpr.components.scheduler.executor.impl.mpc.utils.MpcUtils;
import com.webank.wedpr.components.scheduler.executor.impl.psi.model.PSIJobParam;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MPCExecutorHook implements ExecutorHook {

    private static final Logger logger = LoggerFactory.getLogger(MPCExecutorHook.class);

    private final FileStorageInterface storage;
    private final FileMetaBuilder fileMetaBuilder;

    public MPCExecutorHook(FileStorageInterface storage, FileMetaBuilder fileMetaBuilder) {
        this.storage = storage;
        this.fileMetaBuilder = fileMetaBuilder;
    }

    @Override
    public Object prepare(JobDO jobDO) throws Exception {

        // get the jobParam
        MPCJobParam jobParam = (MPCJobParam) jobDO.getJobParam();
        jobParam.check();

        String agency = WeDPRCommonConfig.getAgency();
        DatasetInfo selfDataset = jobParam.getSelfDataset();
        // int selfIndex = jobParam.getSelfIndex();
        if (selfDataset == null) {
            logger.warn(
                    "self agency does not belong to the party, jobId: {}, agency: {}",
                    jobDO.getId(),
                    agency);
            // self dataset not exist ?
            return null;
        }

        if (jobParam.isNeedRunPsi()) {
            return prepareWithPsi(jobDO, jobParam);
        } else {
            return prepareWithoutPsi(jobDO, jobParam);
        }
    }

    public MpcRunJobRequest buildJobRequest(
            String taskID, String inputFilePath, String outputFilePath, MPCJobParam jobParam) {
        Boolean mpcIsMalicious = MPCExecutorConfig.getMpcIsMalicious();
        int mpcDirectNodePort = MPCExecutorConfig.getMpcDirectNodePort();

        MpcRunJobRequest mpcRunJobRequest = new MpcRunJobRequest();
        mpcRunJobRequest.setJobId(taskID);
        mpcRunJobRequest.setParticipantCount(jobParam.getDataSetList().size());
        mpcRunJobRequest.setSelfIndex(jobParam.getSelfIndex());
        mpcRunJobRequest.setMalicious(mpcIsMalicious);
        mpcRunJobRequest.setMpcNodeDirectPort(mpcDirectNodePort);
        mpcRunJobRequest.setBitLength(jobParam.getShareBytesLength());

        mpcRunJobRequest.setInputFileName(inputFilePath);
        mpcRunJobRequest.setOutputFileName(outputFilePath);

        if (logger.isDebugEnabled()) {
            logger.debug("mpc job request: {}", mpcRunJobRequest);
        }

        return mpcRunJobRequest;
    }

    public MpcRunJobRequest prepareWithoutPsi(JobDO jobDO, MPCJobParam mpcJobParam)
            throws Exception {

        String jobID = jobDO.getId();
        DatasetInfo selfDataset = mpcJobParam.getSelfDataset();
        int selfIndex = mpcJobParam.getSelfIndex();

        String mpcContent = mpcJobParam.getMpcContent();
        FileMeta dataset = selfDataset.getDataset();
        String owner = selfDataset.getDataset().getOwner();

        String datasetFilePath =
                Common.joinPath(
                        ExecutorConfig.getJobCacheDir(jobID),
                        Common.getFileName(dataset.getPath()));

        String mpcContentFilePath =
                Common.joinPath(ExecutorConfig.getJobCacheDir(jobID), jobID + ".mpc");

        String mpcPrepareFilePath =
                Common.joinPath(
                        ExecutorConfig.getJobCacheDir(jobID),
                        ExecutorConfig.getMpcPrepareFileName());

        try {
            int shareBytesLength = MpcUtils.getShareBytesLength(mpcContent);
            int datasetColumnCount = MpcUtils.getDatasetColumnCount(mpcContent, shareBytesLength);

            logger.info(
                    "begin to download dataset file from {} => {}, jobId: {}",
                    selfDataset.getDataset().getStoragePath(),
                    datasetFilePath,
                    jobID);

            // download dataset file
            storage.download(selfDataset.getDataset().getStoragePath(), datasetFilePath);

            logger.info("download dataset file successfully, jobId: {}", jobID);

            // convert dataset to .mpc file
            long mpcDatasetRecordCount =
                    MpcUtils.makeDatasetToMpcDataDirect(
                            datasetFilePath, mpcPrepareFilePath, datasetColumnCount, false);

            // upload mpc prepare file
            String mpcPrepareStoragePath =
                    WeDPRCommonConfig.getUserJobCachePath(
                            owner,
                            JobType.MPC.getType(),
                            jobID,
                            ExecutorConfig.getMpcPrepareFileName());

            FileMeta fileMeta =
                    fileMetaBuilder.build(
                            storage.type(),
                            storage.generateAbsoluteDir(mpcPrepareStoragePath),
                            owner,
                            WeDPRCommonConfig.getAgency());
            logger.info(
                    "begin to upload the mpc prepare file from {}=>{}, jobId: {}",
                    mpcPrepareFilePath,
                    fileMeta.getPath(),
                    jobDO);

            storage.upload(
                    new FileStorageInterface.FilePermissionInfo(owner, null),
                    Boolean.TRUE,
                    mpcPrepareFilePath,
                    fileMeta.getPath(),
                    true);

            logger.info("upload the mpc prepare file successfully, jobId: {}", jobID);

            String newMpcContent =
                    MpcUtils.replaceMpcContentFieldHolder(
                            mpcContent, mpcDatasetRecordCount, mpcJobParam.getDataSetList());

            // upload mpc content
            Files.write(Paths.get(mpcContentFilePath), newMpcContent.getBytes());

            String mpcFileStoragePath =
                    WeDPRCommonConfig.getUserJobCachePath(
                            owner, JobType.MPC.getType(), jobID, jobID + ".mpc");

            FileMeta mpcFileMeta =
                    fileMetaBuilder.build(
                            storage.type(),
                            storage.generateAbsoluteDir(mpcFileStoragePath),
                            owner,
                            WeDPRCommonConfig.getAgency());

            logger.info(
                    "begin to upload the mpc content file from {}=>{}, jobId: {}",
                    mpcContentFilePath,
                    mpcFileMeta.getPath(),
                    jobDO);

            // upload mpc prepare to storage
            storage.upload(
                    new FileStorageInterface.FilePermissionInfo(owner, null),
                    Boolean.TRUE,
                    mpcContentFilePath,
                    mpcFileMeta.getPath(),
                    true);

            logger.info("upload the mpc content file successfully, jobId: {}", jobID);

            String mpcInputFilePath = mpcPrepareStoragePath;

            // upload mpc prepare file
            String mpcOutputFilePath =
                    WeDPRCommonConfig.getUserJobCachePath(
                            owner,
                            JobType.MPC.getType(),
                            jobID,
                            ExecutorConfig.getMpcResultFileName());

            return buildJobRequest(
                    jobDO.getTaskID(), mpcInputFilePath, mpcOutputFilePath, mpcJobParam);
        } finally {
            Common.deleteFile(new File(datasetFilePath));
            Common.deleteFile(new File(mpcPrepareFilePath));
            Common.deleteFile(new File(mpcContentFilePath));
        }
    }

    public MpcRunJobRequest prepareWithPsi(JobDO jobDO, MPCJobParam mpcJobParam) throws Exception {

        String jobID = jobDO.getId();
        DatasetInfo selfDataset = mpcJobParam.getSelfDataset();
        // int selfIndex = mpcJobParam.getSelfIndex();

        String mpcContent = mpcJobParam.getMpcContent();
        FileMeta dataset = selfDataset.getDataset();
        String owner = selfDataset.getDataset().getOwner();

        int shareBytesLength = MpcUtils.getShareBytesLength(mpcContent);
        int datasetColumnCount = MpcUtils.getDatasetColumnCount(mpcContent, shareBytesLength);

        String datasetFilePath =
                Common.joinPath(
                        ExecutorConfig.getJobCacheDir(jobID),
                        Common.getFileName(dataset.getPath()));

        String psiResultFilePath =
                Common.joinPath(
                        ExecutorConfig.getJobCacheDir(jobID),
                        ExecutorConfig.getPsiResultFileName());

        String mpcContentFilePath =
                Common.joinPath(ExecutorConfig.getJobCacheDir(jobID), jobID + ".mpc");

        String mpcPrepareFilePath =
                Common.joinPath(
                        ExecutorConfig.getJobCacheDir(jobID),
                        ExecutorConfig.getMpcPrepareFileName());

        PSIJobParam psiJobParam = mpcJobParam.toPSIJobParam(fileMetaBuilder);
        FileMeta psiResultStoragePath = psiJobParam.getResultPath(fileMetaBuilder, jobID);

        if (logger.isDebugEnabled()) {
            logger.debug("psi result storage path: {}", psiResultStoragePath.getStoragePath());
        }

        try {
            // download dataset file
            logger.info(
                    "begin to download dataset file from {} => {}, jobId: {}",
                    selfDataset.getDataset().getStoragePath(),
                    datasetFilePath,
                    jobID);

            storage.download(selfDataset.getDataset().getStoragePath(), datasetFilePath);

            logger.info("download dataset file successfully, jobId: {}", jobID);

            // download psi result file
            logger.info(
                    "begin to download psi result file from {} => {}, jobId: {}",
                    null,
                    psiResultFilePath,
                    jobID);

            storage.download(psiResultStoragePath.getStoragePath(), psiResultFilePath);

            logger.info("download psi result file successfully, jobId: {}", jobID);

            // merge and sort: psi_result_file and dataset_file
            long mpcDatasetRecordCount =
                    MpcUtils.mergeAndSortById(
                            datasetFilePath,
                            psiResultFilePath,
                            mpcPrepareFilePath,
                            datasetColumnCount,
                            false);

            // upload mpc prepare file
            String mpcPrepareStoragePath =
                    WeDPRCommonConfig.getUserJobCachePath(
                            owner,
                            JobType.MPC.getType(),
                            jobID,
                            ExecutorConfig.getMpcPrepareFileName());

            FileMeta fileMeta =
                    fileMetaBuilder.build(
                            storage.type(),
                            storage.generateAbsoluteDir(mpcPrepareStoragePath),
                            owner,
                            WeDPRCommonConfig.getAgency());
            logger.info(
                    "begin to upload the mpc prepare file from {}=>{}, jobId: {}",
                    mpcPrepareFilePath,
                    fileMeta.getPath(),
                    jobDO);

            storage.upload(
                    new FileStorageInterface.FilePermissionInfo(owner, null),
                    Boolean.TRUE,
                    mpcPrepareFilePath,
                    fileMeta.getPath(),
                    true);

            logger.info("upload the mpc prepare file successfully, jobId: {}", jobID);

            String newMpcContent =
                    MpcUtils.replaceMpcContentFieldHolder(
                            mpcContent, mpcDatasetRecordCount, mpcJobParam.getDataSetList());

            // upload mpc content
            Files.write(Paths.get(mpcContentFilePath), newMpcContent.getBytes());

            String mpcFileStoragePath =
                    WeDPRCommonConfig.getUserJobCachePath(
                            owner, JobType.MPC.getType(), jobID, jobID + ".mpc");

            FileMeta mpcFileMeta =
                    fileMetaBuilder.build(
                            storage.type(),
                            storage.generateAbsoluteDir(mpcFileStoragePath),
                            owner,
                            WeDPRCommonConfig.getAgency());

            logger.info(
                    "begin to upload the mpc content file from {}=>{}, jobId: {}",
                    mpcContentFilePath,
                    mpcFileMeta.getPath(),
                    jobDO);

            // upload mpc prepare to storage
            storage.upload(
                    new FileStorageInterface.FilePermissionInfo(owner, null),
                    Boolean.TRUE,
                    mpcContentFilePath,
                    mpcFileMeta.getPath(),
                    true);

            logger.info("upload the mpc content file successfully, jobId: {}", jobID);

            String mpcInputFilePath = mpcPrepareStoragePath;

            // upload mpc prepare file
            String mpcOutputFilePath =
                    WeDPRCommonConfig.getUserJobCachePath(
                            owner,
                            JobType.MPC.getType(),
                            jobID,
                            ExecutorConfig.getMpcResultFileName());

            return buildJobRequest(
                    jobDO.getTaskID(), mpcInputFilePath, mpcOutputFilePath, mpcJobParam);
        } finally {
            Common.deleteFile(new File(datasetFilePath));
            Common.deleteFile(new File(mpcPrepareFilePath));
            Common.deleteFile(new File(mpcContentFilePath));
            Common.deleteFile(new File(psiResultFilePath));
        }
    }
}
