package com.webank.wedpr.components.scheduler.executor.impl.mpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.webank.wedpr.common.config.WeDPRCommonConfig;
import com.webank.wedpr.common.protocol.JobType;
import com.webank.wedpr.common.protocol.StorageType;
import com.webank.wedpr.common.utils.Common;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.db.mapper.dataset.mapper.DatasetMapper;
import com.webank.wedpr.components.scheduler.executor.impl.model.DatasetInfo;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMeta;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMetaBuilder;
import com.webank.wedpr.components.scheduler.executor.impl.mpc.utils.MpcUtils;
import com.webank.wedpr.components.scheduler.executor.impl.psi.model.PSIJobParam;
import com.webank.wedpr.components.scheduler.python.MpcCodeTranslator;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
public class MPCJobParam {

    private static final Logger logger = LoggerFactory.getLogger(MPCJobParam.class);

    @JsonIgnore private transient String jobID;
    @JsonIgnore private transient JobType jobType;
    @JsonIgnore private transient String submitJobAgency;

    private String sql;
    private String mpcContent;
    private boolean needRunPsi = false;
    private int shareBytesLength;

    // the dataset information
    private List<DatasetInfo> dataSetList;

    @JsonIgnore private boolean receiveResult = false;
    @JsonIgnore private DatasetInfo selfDataset;
    @JsonIgnore private int selfIndex = -1;
    @JsonIgnore private transient List<String> datasetIDList;

    public void check(DatasetMapper datasetMapper) throws Exception {

        String agency = WeDPRCommonConfig.getAgency();

        if (dataSetList == null || dataSetList.isEmpty()) {
            throw new WeDPRException("Invalid mpc job param, must define the dataSet information!");
        }

        //        if (this.jobType == null) {
        //            throw new WeDPRException("Invalid mpc job param, must define the job type!");
        //        }

        if (Common.isEmptyStr(sql) && Common.isEmptyStr(mpcContent)) {
            // sql and mpc content is both empty
            throw new WeDPRException("Invalid mpc job param, must define the mpc code or sql!");
        }

        if (!Common.isEmptyStr(sql)) {
            this.mpcContent = MpcCodeTranslator.translateSqlToMpcCode(sql);

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "trans sql to mpc code, jobId: {}, sql: {}, mpc code: {}",
                        jobID,
                        sql,
                        mpcContent);
            }
        }

        this.mpcContent = this.mpcContent.replace("\r\n", "\n");

        this.shareBytesLength = MpcUtils.getShareBytesLength(mpcContent);
        this.needRunPsi = MpcUtils.checkNeedRunPsi(jobID, mpcContent);

        int index = 0;
        for (DatasetInfo datasetInfo : dataSetList) {

            datasetInfo.setDatasetIDList(datasetIDList);
            datasetInfo.getDataset().obtainDatasetInfo(datasetMapper);
            datasetInfo.check();

            if (index == 0) {
                // String owner = datasetInfo.getDataset().getOwner();
                this.submitJobAgency = datasetInfo.getDataset().getOwnerAgency();
                logger.info("submit mpc job agency, jobId: {}, agency: {}", jobID, agency);
            }

            String ownerAgency = datasetInfo.getDataset().getOwnerAgency();
            if (agency.equals(ownerAgency)) {
                selfDataset = datasetInfo;
                selfIndex = index;
                receiveResult = datasetInfo.getReceiveResult();
            }

            index++;
        }
        if (this.selfDataset == null) {
            throw new WeDPRException("Must define the selfDataset!");
        }

        logger.info(
                "## check params, selfIndex: {}, selfDataset: {}, shareBytesLength: {}, needRunPsi: {}, receiveResult: {}",
                selfIndex,
                selfDataset.getDataset().getDatasetID(),
                shareBytesLength,
                needRunPsi,
                receiveResult);
    }

    public FileMeta getMpcPath(FileMetaBuilder fileMetaBuilder, String jobId, String fileName) {
        if (this.selfDataset == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("self dataset not exist, jobId: {}", jobId);
            }
            return null;
        }

        FileMeta dataset = selfDataset.getDataset();
        String ownerAgency = dataset.getOwnerAgency();
        String owner = dataset.getOwner();
        StorageType storageType = dataset.getStorageType();

        String mpcResultFilePath =
                WeDPRCommonConfig.getUserJobCachePath(
                        owner, JobType.MPC.getType(), jobId, fileName);

        FileMeta fileMeta =
                fileMetaBuilder.build(storageType, mpcResultFilePath, owner, ownerAgency);
        fileMetaBuilder.getAbsoluteDir(fileMeta);

        if (logger.isDebugEnabled()) {
            logger.debug("mpc job params get result file: {}", fileMeta);
        }

        return fileMeta;
    }

    public PSIJobParam toPSIJobParam(FileMetaBuilder fileMetaBuilder) throws Exception {
        PSIJobParam psiJobParam = new PSIJobParam();
        psiJobParam.setJobID(jobID);
        List<PSIJobParam.PartyResourceInfo> partyResourceInfos = new ArrayList<>();
        for (DatasetInfo datasetInfo : dataSetList) {
            PSIJobParam.PartyResourceInfo partyResourceInfo =
                    new PSIJobParam.PartyResourceInfo(datasetInfo.getDataset());
            if (datasetInfo
                    .getDataset()
                    .getOwnerAgency()
                    .equalsIgnoreCase(WeDPRCommonConfig.getAgency())) {
                FileMeta output =
                        PSIJobParam.getDefaultPSIOutputPath(
                                fileMetaBuilder, selfDataset.getDataset(), jobID);
                partyResourceInfo.setOutput(output);
                psiJobParam.setSelfDatasetInfo(partyResourceInfo);
            }
            partyResourceInfo.setIdFields(datasetInfo.getIdFields());
            partyResourceInfo.setReceiveResult(datasetInfo.getReceiveResult());
            partyResourceInfos.add(partyResourceInfo);
        }
        psiJobParam.setPartyResourceInfoList(partyResourceInfos);
        return psiJobParam;
    }

    public String serialize() throws Exception {
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
    }

    public static MPCJobParam deserialize(String data) throws Exception {
        return ObjectMapperFactory.getObjectMapper().readValue(data, MPCJobParam.class);
    }

    @Override
    public String toString() {
        return "MPCJobParam{"
                + "jobID='"
                + jobID
                + '\''
                + ", jobType="
                + jobType
                + ", sql='"
                + sql
                + '\''
                + ", needRunPsi="
                + needRunPsi
                + ", shareBytesLength="
                + shareBytesLength
                + ", dataSetList="
                + dataSetList
                + ", receiveResult="
                + receiveResult
                + ", selfDataset="
                + selfDataset
                + ", selfIndex="
                + selfIndex
                + ", datasetIDList="
                + datasetIDList
                + '}';
    }
}
