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
import com.webank.wedpr.components.scheduler.executor.impl.mpc.utils.MpcUtils;
import com.webank.wedpr.components.scheduler.executor.impl.psi.model.PSIJobParam;
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

    private String sql;
    private String mpcContent;
    private boolean needRunPsi = false;
    private int shareBytesLength;

    // the dataset information
    private List<DatasetInfo> dataSetList;

    @JsonIgnore private DatasetInfo selfDataset;
    @JsonIgnore private int selfIndex = -1;
    @JsonIgnore private transient List<String> datasetIDList;

    public void check() throws Exception {

        String agency = WeDPRCommonConfig.getAgency();

        if (dataSetList == null || dataSetList.isEmpty()) {
            throw new WeDPRException("Invalid mpc job param, must define the dataSet information!");
        }
        if (this.jobType == null) {
            throw new WeDPRException("Invalid mpc job param, must define the job type!");
        }

        if (Common.isEmptyStr(sql) && Common.isEmptyStr(mpcContent)) {
            // sql and mpc content is both empty
            throw new WeDPRException("Invalid mpc job param, must define the mpc code or sql!");
        }

        if (!Common.isEmptyStr(sql)) {
            this.mpcContent = MpcUtils.transSql2MpcCode(sql);

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "trans sql to mpc code, jobId: {}, sql: {}, mpc code: {}",
                        jobID,
                        sql,
                        mpcContent);
            }
        }

        this.shareBytesLength = MpcUtils.getShareBytesLength(mpcContent);
        this.needRunPsi = MpcUtils.checkNeedRunPsi(jobID, mpcContent);

        int index = 0;
        for (DatasetInfo datasetInfo : dataSetList) {
            datasetInfo.setDatasetIDList(datasetIDList);
            datasetInfo.check();

            String ownerAgency = datasetInfo.getDataset().getOwnerAgency();
            if (agency.equals(ownerAgency)) {
                selfDataset = datasetInfo;
                selfIndex = index;
            }

            index++;
        }

        logger.info(
                "## check params, selfIndex: {}, selfDataset: {}, shareBytesLength: {}, needRunPsi: {}",
                selfIndex,
                selfDataset,
                shareBytesLength,
                needRunPsi);
    }

    public PSIJobParam toPSIJobParam(FileMetaBuilder fileMetaBuilder) {
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

        if (logger.isDebugEnabled()) {
            logger.debug("to psi params, psiJobParam: {}", psiJobParam);
        }

        return psiJobParam;
    }

    public String serialize() throws Exception {
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
    }

    public static MPCJobParam deserialize(String data) throws Exception {
        return ObjectMapperFactory.getObjectMapper().readValue(data, MPCJobParam.class);
    }
}
