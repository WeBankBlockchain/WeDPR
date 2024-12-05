package com.webank.wedpr.components.scheduler.executor.hook;

import com.webank.wedpr.common.config.WeDPRCommonConfig;
import com.webank.wedpr.common.utils.Common;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.db.mapper.dataset.mapper.DatasetMapper;
import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.scheduler.core.SpdzConnections;
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
    private final DatasetMapper datasetMapper;
    private final SpdzConnections spdzConnections;

    public MPCExecutorHook(
            DatasetMapper datasetMapper,
            FileStorageInterface storage,
            FileMetaBuilder fileMetaBuilder,
            SpdzConnections spdzConnections) {
        this.datasetMapper = datasetMapper;
        this.storage = storage;
        this.fileMetaBuilder = fileMetaBuilder;
        this.spdzConnections = spdzConnections;
    }

    @Override
    public Object prepare(JobDO jobDO) throws Exception {

        // get the jobParam
        MPCJobParam jobParam = (MPCJobParam) jobDO.getJobParam();

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

        return prepare(jobDO, jobParam);
    }

    public MpcRunJobRequest buildJobRequest(
            String taskID,
            String owner,
            String mpcFilePath,
            String inputFilePath,
            String outputFilePath,
            String resultFilePath,
            MPCJobParam jobParam)
            throws WeDPRException {

        logger.debug(
                " ## build mpc job request, taskID: {}, mpcFilePath: {}, inputFile: {}, outputFile: {}",
                taskID,
                mpcFilePath,
                inputFilePath,
                outputFilePath);

        Boolean mpcIsMalicious = MPCExecutorConfig.getMpcIsMalicious();

        SpdzConnections.Connection connection =
                spdzConnections.getConnection(jobParam.getSubmitJobAgency());

        String mpcDirectNodeIp = connection.getIp();
        int mpcDirectNodePort = connection.getPort();

        MpcRunJobRequest mpcRunJobRequest = new MpcRunJobRequest();
        mpcRunJobRequest.setJobId(taskID);
        mpcRunJobRequest.setParticipantCount(jobParam.getDataSetList().size());
        mpcRunJobRequest.setSelfIndex(jobParam.getSelfIndex());
        mpcRunJobRequest.setMalicious(mpcIsMalicious);
        mpcRunJobRequest.setReceiverNodeIp(mpcDirectNodeIp);
        mpcRunJobRequest.setMpcNodeDirectPort(mpcDirectNodePort);
        mpcRunJobRequest.setBitLength(jobParam.getShareBytesLength());
        mpcRunJobRequest.setReceiveResult(jobParam.isReceiveResult());

        mpcRunJobRequest.setMpcFilePath(mpcFilePath);
        mpcRunJobRequest.setInputFilePath(inputFilePath);
        mpcRunJobRequest.setOutputFilePath(outputFilePath);
        mpcRunJobRequest.setResultFilePath(resultFilePath);
        mpcRunJobRequest.setOwner(owner);

        logger.info(" ## mpc job request: {}", mpcRunJobRequest);

        return mpcRunJobRequest;
    }

    public MpcRunJobRequest prepare(JobDO jobDO, MPCJobParam mpcJobParam) throws WeDPRException {

        String jobId = jobDO.getId();

        DatasetInfo selfDataset = mpcJobParam.getSelfDataset();
        String owner = selfDataset.getDataset().getOwner();

        FileMeta mpcPrepareFileMeta =
                mpcJobParam.getMpcPath(
                        fileMetaBuilder, jobId, ExecutorConfig.getMpcPrepareFileName());
        FileMeta mpcContentFileMeta =
                mpcJobParam.getMpcPath(
                        fileMetaBuilder, jobId, ExecutorConfig.getMpcFileName(jobId));
        FileMeta mpcOutputFileMeta =
                mpcJobParam.getMpcPath(
                        fileMetaBuilder, jobId, ExecutorConfig.getMpcOutputFileName());
        FileMeta mpcResultFileMeta =
                mpcJobParam.getMpcPath(
                        fileMetaBuilder, jobId, ExecutorConfig.getMpcResultFileName());

        return buildJobRequest(
                jobDO.getTaskID(),
                owner,
                mpcContentFileMeta.getPath(),
                mpcPrepareFileMeta.getPath(),
                mpcOutputFileMeta.getPath(),
                mpcResultFileMeta.getPath(),
                mpcJobParam);
    }

    public void prepareWithoutPsi(DatasetMapper datasetMapper, JobDO jobDO, MPCJobParam mpcJobParam)
            throws Exception {

        String jobID = jobDO.getId();

        logger.info("prepare step begin, jobId: {}", jobDO.getId());

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

        FileMeta mpcPrepareFileMeta =
                mpcJobParam.getMpcPath(
                        fileMetaBuilder, jobID, ExecutorConfig.getMpcPrepareFileName());
        FileMeta mpcContentFileMeta =
                mpcJobParam.getMpcPath(fileMetaBuilder, jobID, jobID + ".mpc");
        //        FileMeta mpcOutputFileMeta =
        //                mpcJobParam.getMpcPath(
        //                        fileMetaBuilder, jobID, ExecutorConfig.getMpcOutputFileName());
        //        FileMeta mpcResultFileMeta =
        //                mpcJobParam.getMpcPath(
        //                        fileMetaBuilder, jobID, ExecutorConfig.getMpcResultFileName());

        try {
            // int shareBytesLength = MpcUtils.getShareBytesLength(mpcContent);
            int datasetColumnCount = MpcUtils.getMpcDatasetColumnCount(mpcContent, selfIndex);

            logger.info(
                    "prepare step, obtain the fields number the dataset participating in mpc task, jobId: {}, fieldNumber: {}",
                    jobID,
                    datasetColumnCount);

            logger.info(
                    "prepare step, begin to download dataset file from {} => {}, jobId: {}",
                    selfDataset.getDataset().getStoragePath(),
                    datasetFilePath,
                    jobID);

            // download dataset file
            storage.download(selfDataset.getDataset().getStoragePath(), datasetFilePath);

            logger.info("prepare step, download dataset file successfully, jobId: {}", jobID);

            // convert dataset to .mpc file
            MpcUtils.makeDatasetToMpcDataDirect(
                    jobID, datasetFilePath, mpcPrepareFilePath, datasetColumnCount, false);

            logger.info(
                    "prepare step, begin to upload the mpc prepare file from {}=>{}, jobId: {}",
                    mpcPrepareFilePath,
                    mpcPrepareFileMeta.getPath(),
                    jobDO);

            storage.upload(
                    new FileStorageInterface.FilePermissionInfo(owner, null),
                    Boolean.TRUE,
                    mpcPrepareFilePath,
                    mpcPrepareFileMeta.getPath(),
                    true);

            logger.info("prepare step, upload the mpc prepare file successfully, jobId: {}", jobID);

            String newMpcContent =
                    MpcUtils.replaceMpcContentFieldHolder(mpcContent, mpcJobParam.getDataSetList());

            // upload mpc content
            Files.write(Paths.get(mpcContentFilePath), newMpcContent.getBytes());

            logger.info(
                    "prepare step, begin to upload the mpc content file from {}=>{}, jobId: {}",
                    mpcContentFilePath,
                    mpcContentFileMeta.getPath(),
                    jobDO);

            // upload mpc prepare to storage
            storage.upload(
                    new FileStorageInterface.FilePermissionInfo(owner, null),
                    Boolean.TRUE,
                    mpcContentFilePath,
                    mpcContentFileMeta.getPath(),
                    true);

            logger.info("prepare step, upload the mpc content file successfully, jobId: {}", jobID);

        } catch (Exception e) {
            logger.error("e: ", e);
            throw e;
        } finally {
            Common.deleteFile(new File(datasetFilePath));
            Common.deleteFile(new File(mpcPrepareFilePath));
            Common.deleteFile(new File(mpcContentFilePath));

            logger.info("prepare step end, jobId: {}", jobDO.getId());
        }
    }

    public void prepareWithPsi(DatasetMapper datasetMapper, JobDO jobDO, MPCJobParam mpcJobParam)
            throws Exception {

        String jobID = jobDO.getId();

        long startTimeMillis = System.currentTimeMillis();

        logger.info("prepare step(psi) begin, jobId: {}", jobID);

        DatasetInfo selfDataset = mpcJobParam.getSelfDataset();
        int selfIndex = mpcJobParam.getSelfIndex();

        String mpcContent = mpcJobParam.getMpcContent();
        FileMeta dataset = selfDataset.getDataset();
        String owner = selfDataset.getDataset().getOwner();

        // int shareBytesLength = MpcUtils.getShareBytesLength(mpcContent);
        int datasetColumnCount = MpcUtils.getMpcDatasetColumnCount(mpcContent, selfIndex);

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
        FileMeta psiResultStoragePath =
                psiJobParam.getResultPath(datasetMapper, fileMetaBuilder, jobID);

        logger.info(
                "prepare step(psi), fetch psi result storage path: {}",
                psiResultStoragePath.getStoragePath());

        FileMeta mpcPrepareFileMeta =
                mpcJobParam.getMpcPath(
                        fileMetaBuilder, jobID, ExecutorConfig.getMpcPrepareFileName());
        FileMeta mpcContentFileMeta =
                mpcJobParam.getMpcPath(
                        fileMetaBuilder, jobID, ExecutorConfig.getMpcFileName(jobID));
        //        FileMeta mpcOutputFileMeta =
        //                mpcJobParam.getMpcPath(
        //                        fileMetaBuilder, jobID, ExecutorConfig.getMpcOutputFileName());
        //        FileMeta mpcResultFileMeta =
        //                mpcJobParam.getMpcPath(
        //                        fileMetaBuilder, jobID, ExecutorConfig.getMpcResultFileName());

        try {
            // download dataset file
            logger.info(
                    "prepare step(psi), begin to download dataset file from {} => {}, jobId: {}",
                    selfDataset.getDataset().getStoragePath(),
                    datasetFilePath,
                    jobID);

            storage.download(selfDataset.getDataset().getStoragePath(), datasetFilePath);

            // download psi result file
            logger.info(
                    "prepare step(psi), begin to download psi result file from {} => {}, jobId: {}",
                    psiResultStoragePath.getStoragePath(),
                    psiResultFilePath,
                    jobID);

            storage.download(psiResultStoragePath.getStoragePath(), psiResultFilePath);

            logger.info(
                    "prepare step(psi), download psi result file successfully, jobId: {}", jobID);

            // merge and sort: psi_result_file and dataset_file
            long mpcDatasetRecordCount =
                    MpcUtils.mergeAndSortById(
                            jobID,
                            datasetFilePath,
                            psiResultFilePath,
                            mpcPrepareFilePath,
                            datasetColumnCount,
                            false);

            logger.info(
                    "prepare step(psi), begin to upload the mpc prepare file from {}=>{}, jobId: {}",
                    mpcPrepareFilePath,
                    mpcPrepareFileMeta.getPath(),
                    jobDO);

            storage.upload(
                    new FileStorageInterface.FilePermissionInfo(owner, null),
                    Boolean.TRUE,
                    mpcPrepareFilePath,
                    mpcPrepareFileMeta.getPath(),
                    true);

            logger.info(
                    "prepare step(psi), upload the mpc prepare file successfully, jobId: {}",
                    jobID);

            String newMpcContent =
                    MpcUtils.replaceMpcContentFieldHolder(
                            mpcContent, mpcJobParam.getDataSetList().size(), mpcDatasetRecordCount);

            // upload mpc content
            Files.write(Paths.get(mpcContentFilePath), newMpcContent.getBytes());

            logger.info(
                    "prepare step(psi), begin to upload the mpc content file from {}=>{}, jobId: {}",
                    mpcContentFilePath,
                    mpcContentFileMeta.getPath(),
                    jobDO);

            // upload mpc prepare to storage
            storage.upload(
                    new FileStorageInterface.FilePermissionInfo(owner, null),
                    Boolean.TRUE,
                    mpcContentFilePath,
                    mpcContentFileMeta.getPath(),
                    true);

            logger.info(
                    "prepare step(psi), upload the mpc content file successfully, jobId: {}",
                    jobID);

        } finally {
            Common.deleteFile(new File(datasetFilePath));
            Common.deleteFile(new File(mpcPrepareFilePath));
            Common.deleteFile(new File(mpcContentFilePath));
            Common.deleteFile(new File(psiResultFilePath));

            long endTimeMillis = System.currentTimeMillis();
            logger.info(
                    "prepare step(psi) end, jobId: {}, costMs: {}",
                    jobID,
                    endTimeMillis - startTimeMillis);
        }
    }
}
