package com.webank.wedpr.components.scheduler.executor.impl.mpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.webank.wedpr.components.scheduler.client.SchedulerClient;
import com.webank.wedpr.components.scheduler.executor.impl.model.DatasetInfo;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMeta;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMetaBuilder;
import com.webank.wedpr.components.scheduler.executor.impl.mpc.request.MPCJobRequest;
import com.webank.wedpr.components.scheduler.executor.impl.psi.model.PSIJobParam;
import com.webank.wedpr.components.scheduler.executor.impl.psi.model.PSIRequest;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import com.webank.wedpr.core.config.WeDPRCommonConfig;
import com.webank.wedpr.core.protocol.JobType;
import com.webank.wedpr.core.utils.Common;
import com.webank.wedpr.core.utils.ObjectMapperFactory;
import com.webank.wedpr.core.utils.WeDPRException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MPCJobParam {

    private static final Logger logger = LoggerFactory.getLogger(MPCJobParam.class);

    public static final int DEFAULT_MPC_SHARE_BYTES_LENGTH = 64;

    @JsonIgnore private transient String jobID;
    @JsonIgnore private transient JobType jobType;
    @JsonIgnore private transient DatasetInfo selfDataset;
    @JsonIgnore private transient int selfIndex;

    private String sql;
    private String mpcContent;
    private boolean needRunPSI;
    private int shareBytesLength;

    // TODO:
    private String workspace;
    private String jobCacheDir;

    /*
    self.workspace = workspace
    self.job_cache_dir = "{}{}{}".format(self.workspace, os.sep, self.job_id)
    self.dataset_file_path = "{}{}{}".format(self.job_cache_dir, os.sep, self.dataset_id)
    self.psi_prepare_path = "{}{}{}".format(self.job_cache_dir, os.sep, JobContext.PSI_PREPARE_FILE)
    self.psi_result_index_path = "{}{}{}".format(self.job_cache_dir, os.sep, JobContext.PSI_RESULT_INDEX_FILE)
    self.psi_result_path = "{}{}{}".format(self.job_cache_dir, os.sep, JobContext.PSI_RESULT_FILE)
    self.mpc_file_name = "{}.mpc".format(self.job_id)
    self.mpc_model_module_name = "{}.json".format(self.job_id)
    self.mpc_file_path = "{}{}{}".format(self.job_cache_dir, os.sep, self.mpc_file_name)
    self.mpc_prepare_path = "{}{}{}".format(self.job_cache_dir, os.sep, JobContext.MPC_PREPARE_FILE)
    self.mpc_result_path = "{}{}{}".format(self.job_cache_dir, os.sep, JobContext.MPC_RESULT_FILE)
    self.mpc_output_path = "{}{}{}".format(self.job_cache_dir, os.sep, JobContext.MPC_OUTPUT_FILE)
    */

    // the dataset information
    private List<DatasetInfo> dataSetList;

    @JsonIgnore private transient List<String> datasetIDList;

    public void transferSQL2MpcCode() throws Exception {
        if (Common.isEmptyStr(sql)) {
            return;
        }

        if (!Common.isEmptyStr(mpcContent)) {
            return;
        }

        SchedulerClient schedulerClient = new SchedulerClient();
        String mpcContent = schedulerClient.transferSQL2MPCContent(jobID, sql);

        this.mpcContent = mpcContent;

        logger.info(
                "transfer sql to mpc code, jobId: {}, sql: {}, mpc code: {}",
                jobID,
                sql,
                mpcContent);
    }

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

    public int getShareBytesLength(String mpcContent) {
        String target = "# BIT_LENGTH = ";
        if (mpcContent.contains(target)) {
            int start = mpcContent.indexOf(target);
            int end = mpcContent.indexOf('\n', start + target.length());
            String bitLengthStr = mpcContent.substring(start + target.length(), end).trim();
            int bitLength = Integer.parseInt(bitLengthStr);
            logger.info("OUTPUT_BIT_LENGTH = {}", bitLength);
            return bitLength;
        } else {
            logger.info("OUTPUT_BIT_LENGTH = 64");
            return DEFAULT_MPC_SHARE_BYTES_LENGTH;
        }
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

        this.transferSQL2MpcCode();
        this.needRunPSI = checkNeedRunPSI();
        this.shareBytesLength = getShareBytesLength(mpcContent);

        String selfAgency = WeDPRCommonConfig.getAgency();

        int index = 0;
        for (DatasetInfo datasetInfo : dataSetList) {
            datasetInfo.setDatasetIDList(datasetIDList);
            datasetInfo.check();

            if (datasetInfo.getDataset().getOwnerAgency().compareToIgnoreCase(selfAgency) == 0) {
                this.selfDataset = datasetInfo;
                this.selfIndex = index;
            }
            //            if (datasetInfo.getReceiveResult()) {
            //                modelRequest
            //                        .getResultReceiverIDList()
            //                        .add(datasetInfo.getDataset().getOwnerAgency());
            //            }

            index++;
        }

        if (this.selfDataset == null) {
            throw new WeDPRException(
                    "Invalid mpc job param, the dataSet for participant agency "
                            + selfAgency
                            + " not set!");
        }
        // TODO:
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
        return null;
    }

    public Object toPSIRequest() {
        PSIRequest psiRequest = new PSIRequest();
        return psiRequest;
    }

    public String serialize() throws Exception {
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
    }

    public static MPCJobParam deserialize(String data) throws Exception {
        return ObjectMapperFactory.getObjectMapper().readValue(data, MPCJobParam.class);
    }
}
