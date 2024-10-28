package com.webank.wedpr.components.scheduler.executor.impl.mpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.webank.wedpr.common.config.WeDPRCommonConfig;
import com.webank.wedpr.common.protocol.JobType;
import com.webank.wedpr.common.utils.Common;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.scheduler.executor.impl.model.DatasetInfo;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMeta;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMetaBuilder;
import com.webank.wedpr.components.scheduler.executor.impl.mpc.request.MPCJobRequest;
import com.webank.wedpr.components.scheduler.executor.impl.psi.model.PSIJobParam;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MPCJobParam {

    private static final Logger logger = LoggerFactory.getLogger(MPCJobParam.class);

    @JsonIgnore private transient String jobID;
    @JsonIgnore private transient JobType jobType;

    private String sql;
    private String mpcContent;

    // the dataset information
    private List<DatasetInfo> dataSetList;

    @JsonIgnore private DatasetInfo selfDataset;
    @JsonIgnore private int selfIndex = -1;

    @JsonIgnore private transient List<String> datasetIDList;

    public boolean checkNeedRunPSI() {

        if (this.mpcContent == null) {
            return false;
        }

        // TODO: make it config item
        String regex = "PSI_OPTION\\s*=\\s*True";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(this.mpcContent);

        return matcher.find();
    }

    public void check() throws Exception {
        if (dataSetList == null || dataSetList.isEmpty()) {
            throw new WeDPRException("Invalid mpc job param, must define the dataSet information!");
        }
        if (this.jobType == null) {
            throw new WeDPRException("Invalid mpc job param, must define the job type!");
        }

        if (Common.isEmptyStr(sql) && Common.isEmptyStr(mpcContent)) {
            // sql and mpcCode is all empty
            throw new WeDPRException("Invalid mpc job param, must define the mpc code or sql!");
        }

        for (DatasetInfo datasetInfo : dataSetList) {
            datasetInfo.setDatasetIDList(datasetIDList);
            datasetInfo.check();
        }
    }

    public PSIJobParam toPSIJobParam(FileMetaBuilder fileMetaBuilder, FileStorageInterface storage)
            throws Exception {
        PSIJobParam psiJobParam = new PSIJobParam();
        psiJobParam.setJobID(jobID);
        List<PSIJobParam.PartyResourceInfo> partyResourceInfos = new ArrayList<>();
        for (DatasetInfo datasetInfo : dataSetList) {
            FileMeta output =
                    PSIJobParam.getDefaultPSIOutputPath(
                            fileMetaBuilder, datasetInfo.getDataset(), jobID);
            PSIJobParam.PartyResourceInfo partyResourceInfo =
                    new PSIJobParam.PartyResourceInfo(datasetInfo.getDataset(), output);
            partyResourceInfo.setIdFields(datasetInfo.getIdFields());
            partyResourceInfo.setReceiveResult(datasetInfo.getReceiveResult());
            partyResourceInfos.add(partyResourceInfo);
        }
        psiJobParam.setPartyResourceInfoList(partyResourceInfos);
        return psiJobParam;
    }

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getMpcContent() {
        return mpcContent;
    }

    public void setMpcContent(String mpcContent) {
        this.mpcContent = mpcContent;
    }

    public List<DatasetInfo> getDataSetList() {
        return dataSetList;
    }

    public void setDataSetList(List<DatasetInfo> dataSetList) {
        this.dataSetList = dataSetList;
    }

    public List<String> getDatasetIDList() {
        return datasetIDList;
    }

    public void setDatasetIDList(List<String> datasetIDList) {
        this.datasetIDList = datasetIDList;
    }

    public Object toMPCJobRequest() {

        MPCJobRequest mpcJobRequest = new MPCJobRequest();
        mpcJobRequest.setJobId(jobID);
        mpcJobRequest.setMpcContent(mpcContent);
        mpcJobRequest.setSql(sql);
        mpcJobRequest.setPartyCount(dataSetList.size());
        mpcJobRequest.setWithPSI(checkNeedRunPSI());

        List<MPCJobRequest.MPCDatasetInfo> mpcDatasetInfos = new ArrayList<>();

        String selfAgency = WeDPRCommonConfig.getAgency();

        int index = 0;
        for (DatasetInfo datasetInfo : dataSetList) {
            datasetInfo.setDatasetIDList(datasetIDList);
            datasetInfo.check();

            if (datasetInfo.getDataset().getOwnerAgency().compareToIgnoreCase(selfAgency) == 0) {
                mpcJobRequest.setSelfIndex(index);
                mpcJobRequest.setReceiveResult(datasetInfo.getReceiveResult());
                mpcJobRequest.setInputFilePath(datasetInfo.getDataset().getPath());
            }

            MPCJobRequest.MPCDatasetInfo mpcDatasetInfo = new MPCJobRequest.MPCDatasetInfo();

            mpcDatasetInfo.setReceiveResult(datasetInfo.getReceiveResult());
            mpcDatasetInfo.setDatasetPath(datasetInfo.getDataset().getPath());
            mpcDatasetInfo.setDatasetRecordCount(datasetInfo.getDataset().getDatasetRecordCount());
            mpcDatasetInfos.add(mpcDatasetInfo);

            index++;
        }
        mpcJobRequest.setDatasetList(mpcDatasetInfos);

        return mpcJobRequest;
    }

    public String serialize() throws Exception {
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
    }

    public static MPCJobParam deserialize(String data) throws Exception {
        return ObjectMapperFactory.getObjectMapper().readValue(data, MPCJobParam.class);
    }
}
