/*
 * Copyright 2017-2025  [webank-wedpr]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.webank.wedpr.components.scheduler.executor.impl.psi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.webank.wedpr.common.config.WeDPRCommonConfig;
import com.webank.wedpr.common.protocol.JobType;
import com.webank.wedpr.common.utils.CSVFileParser;
import com.webank.wedpr.common.utils.Common;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.scheduler.executor.impl.ExecutorConfig;
import com.webank.wedpr.components.scheduler.executor.impl.model.DatasetInfo;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMeta;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMetaBuilder;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PSIJobParam {
    private static final Logger logger = LoggerFactory.getLogger(PSIJobParam.class);

    @SneakyThrows(Exception.class)
    public static FileMeta getDefaultPSIOutputPath(
            FileMetaBuilder fileMetaBuilder, FileMeta input, String jobID) {
        String remotePath = ExecutorConfig.getDefaultPSIResultPath(input.getOwner(), jobID);
        FileMeta output =
                fileMetaBuilder.build(
                        input.getStorageType(),
                        remotePath,
                        input.getOwner(),
                        input.getOwnerAgency());
        fileMetaBuilder.getAbsoluteDir(output);
        return output;
    }

    public static class PartyResourceInfo extends DatasetInfo {
        private FileMeta output;

        public PartyResourceInfo() {}

        public PartyResourceInfo(FileMeta input, FileMeta output) {
            this.dataset = input;
            this.output = output;
        }

        public void checkAndResetPath(FileMetaBuilder fileMetaBuilder, String jobID)
                throws Exception {
            if (dataset == null) {
                throw new WeDPRException("Invalid PSI Request, must define the input dataset!");
            }
            dataset.check(this.datasetIDList);
            if (idFields == null || idFields.isEmpty()) {
                throw new WeDPRException("Must define the field list to run PSI!");
            }
            // set the default output
            if (output == null) {
                this.output = PSIJobParam.getDefaultPSIOutputPath(fileMetaBuilder, dataset, jobID);
            }
        }

        public FileMeta getOutput() {
            return output;
        }

        public void setOutput(FileMeta output) {
            this.output = output;
        }
    }

    private String jobID;
    private String taskID;

    @JsonProperty("dataSetList")
    private List<PartyResourceInfo> partyResourceInfoList;

    @JsonIgnore private List<String> datasetIDList;

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public List<PartyResourceInfo> getPartyResourceInfoList() {
        return partyResourceInfoList;
    }

    public void setPartyResourceInfoList(List<PartyResourceInfo> partyResourceInfoList) {
        this.partyResourceInfoList = partyResourceInfoList;
    }

    public static PSIJobParam deserialize(String data) throws Exception {
        if (StringUtils.isBlank(data)) {
            throw new WeDPRException("The PSIJobParam must be non-empty!");
        }
        return ObjectMapperFactory.getObjectMapper().readValue(data, PSIJobParam.class);
    }

    public FileMeta getResultPath(FileMetaBuilder fileMetaBuilder, String jobID) {
        if (this.partyResourceInfoList == null) {
            return null;
        }
        for (PartyResourceInfo partyResourceInfo : partyResourceInfoList) {
            if (partyResourceInfo.getDataset() == null) {
                continue;
            }
            if (partyResourceInfo
                            .getDataset()
                            .getOwnerAgency()
                            .compareToIgnoreCase(WeDPRCommonConfig.getAgency())
                    == 0) {
                FileMeta output = partyResourceInfo.getOutput();
                if (output == null) {
                    output =
                            PSIJobParam.getDefaultPSIOutputPath(
                                    fileMetaBuilder, partyResourceInfo.getDataset(), jobID);
                }
                return output;
            }
        }
        return null;
    }

    public void check(FileMetaBuilder fileMetaBuilder) throws Exception {
        Common.requireNonEmpty(jobID, "jobID");
        if (partyResourceInfoList == null || partyResourceInfoList.size() < 2) {
            throw new WeDPRException("Invalid PSIJobParam, must define at least 2-parties!");
        }
        for (PartyResourceInfo partyResourceInfo : partyResourceInfoList) {
            partyResourceInfo.setDatasetIDList(datasetIDList);
            partyResourceInfo.checkAndResetPath(fileMetaBuilder, jobID);
        }
    }

    public PSIRequest convert(String ownerAgency) throws Exception {
        PSIRequest psiRequest = new PSIRequest();
        psiRequest.setTaskID(this.taskID);
        psiRequest.setParties(toPSIParam(ownerAgency));
        resetPartyIndex(psiRequest.getParties());
        List<String> receivers = new ArrayList<>();
        boolean syncResult = false;
        for (PartyResourceInfo partyInfo : partyResourceInfoList) {
            if (partyInfo.getReceiveResult()) {
                receivers.add(partyInfo.getDataset().getOwnerAgency());
                syncResult = true;
            }
        }
        psiRequest.setReceiverList(receivers);
        psiRequest.setSyncResult(syncResult);
        return psiRequest;
    }

    private List<PartyInfo> toPSIParam(String ownerAgency) {
        List<PartyInfo> partyInfoList = new ArrayList<>();
        for (PartyResourceInfo party : partyResourceInfoList) {
            String agency = party.getDataset().getOwnerAgency();
            PartyInfo partyInfo = new PartyInfo(agency);
            if (agency.compareToIgnoreCase(ownerAgency) == 0) {
                partyInfo.setPartyIndex(PartyInfo.PartyType.SERVER.getType());
            } else {
                partyInfo.setPartyIndex(PartyInfo.PartyType.CLIENT.getType());
            }
            partyInfo.setData(
                    new PartyInfo.PartyData(jobID, party.getDataset(), party.getOutput()));
            partyInfoList.add(partyInfo);
        }
        return partyInfoList;
    }

    private void resetPartyIndex(List<PartyInfo> partyInfoList) {
        if (partyInfoList.size() == 2) {
            return;
        }
        partyInfoList.get(0).setPartyIndex(PartyInfo.MultiPSIPartyType.CALCULATOR.getType());
        partyInfoList
                .get(partyInfoList.size() - 1)
                .setPartyIndex(PartyInfo.MultiPSIPartyType.MASTER.getType());
        for (int i = 1; i < partyInfoList.size() - 1; i++) {
            partyInfoList.get(i).setPartyIndex(PartyInfo.MultiPSIPartyType.PARTNER.getType());
        }
    }

    // download and extract the psi file
    public void prepare(FileMetaBuilder fileMetaBuilder, FileStorageInterface storage)
            throws Exception {
        for (PartyResourceInfo partyResourceInfo : partyResourceInfoList) {
            if (partyResourceInfo
                            .getDataset()
                            .getOwnerAgency()
                            .compareToIgnoreCase(WeDPRCommonConfig.getAgency())
                    == 0) {
                prepare(partyResourceInfo, fileMetaBuilder, storage);
                break;
            }
        }
    }

    public void prepare(
            PartyResourceInfo partyInfo,
            FileMetaBuilder fileMetaBuilder,
            FileStorageInterface storage)
            throws Exception {
        String downloadedFilePath =
                Common.joinPath(
                        ExecutorConfig.getJobCacheDir(jobID),
                        Common.getFileName(partyInfo.getDataset().getPath()));

        String extractFilePath = ExecutorConfig.getPsiTmpFilePath(jobID);
        try {
            logger.info(
                    "Prepare PSI, job: {}, partyInfo: {}",
                    jobID,
                    partyInfo.getDataset().toString());
            long startT = System.currentTimeMillis();

            // download the input dataset according to the path
            storage.download(partyInfo.getDataset().getStoragePath(), downloadedFilePath);
            logger.info(
                    "Prepare PSI, download file from {}=>{} success, timecost: {}",
                    partyInfo.getDataset().getPath(),
                    downloadedFilePath,
                    System.currentTimeMillis() - startT);
            // extract the fields
            startT = System.currentTimeMillis();
            logger.info("Begin to extract file {} into {}", downloadedFilePath, extractFilePath);
            CSVFileParser.ExtractConfig extractConfig =
                    new CSVFileParser.ExtractConfig(
                            downloadedFilePath, partyInfo.getIdFields(), extractFilePath);
            CSVFileParser.extractFields(extractConfig);
            logger.info(
                    "Extract file {} into {} success, timecost: {}",
                    downloadedFilePath,
                    extractFilePath,
                    System.currentTimeMillis() - startT);

            // upload the psi tmp file
            startT = System.currentTimeMillis();
            String owner = partyInfo.getDataset().getOwner();
            String remotePath =
                    WeDPRCommonConfig.getUserJobCachePath(
                            owner,
                            JobType.PSI.getType(),
                            jobID,
                            ExecutorConfig.getPSIPrepareFileName());

            // update the input
            FileMeta updatedInput =
                    fileMetaBuilder.build(
                            storage.type(),
                            storage.generateAbsoluteDir(remotePath),
                            owner,
                            WeDPRCommonConfig.getAgency());
            logger.info(
                    "Begin to upload the extracted psi file from {}=>{}",
                    extractFilePath,
                    updatedInput.getPath());
            storage.upload(
                    new FileStorageInterface.FilePermissionInfo(owner, null),
                    Boolean.TRUE,
                    extractFilePath,
                    updatedInput.getPath(),
                    false);
            partyInfo.setDataset(updatedInput);

            logger.info(
                    "Upload the extracted psi file from {}=>{} success, timecost: {}",
                    extractFilePath,
                    updatedInput.getPath(),
                    System.currentTimeMillis() - startT);
        } finally {
            // clean the downloaded
            Common.deleteFile(new File(downloadedFilePath));
            Common.deleteFile(new File(extractFilePath));
        }
    }

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public List<String> getDatasetIDList() {
        return datasetIDList;
    }

    public void setDatasetIDList(List<String> datasetIDList) {
        this.datasetIDList = datasetIDList;
    }

    public String serialize() throws Exception {
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
    }
}
