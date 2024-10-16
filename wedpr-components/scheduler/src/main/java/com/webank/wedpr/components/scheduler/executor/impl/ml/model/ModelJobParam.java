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

package com.webank.wedpr.components.scheduler.executor.impl.ml.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.webank.wedpr.common.config.WeDPRCommonConfig;
import com.webank.wedpr.common.protocol.JobType;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.meta.setting.template.dao.SettingTemplateDO;
import com.webank.wedpr.components.scheduler.executor.impl.ml.request.FeatureEngineeringRequest;
import com.webank.wedpr.components.scheduler.executor.impl.ml.request.ModelJobRequest;
import com.webank.wedpr.components.scheduler.executor.impl.ml.request.PreprocessingRequest;
import com.webank.wedpr.components.scheduler.executor.impl.model.AlgorithmType;
import com.webank.wedpr.components.scheduler.executor.impl.model.DatasetInfo;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMeta;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMetaBuilder;
import com.webank.wedpr.components.scheduler.executor.impl.psi.model.PSIJobParam;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModelJobParam {

    @JsonIgnore private transient String jobID;
    @JsonIgnore private transient JobType jobType;
    // the model Setting
    private Object modelSetting;
    // the model parm for predicting
    private String modelPredictAlgorithm;

    // the dataset information
    private List<DatasetInfo> dataSetList;

    @JsonIgnore private transient List<String> datasetIDList;
    @JsonIgnore private transient DatasetInfo selfDataset;
    @JsonIgnore private transient DatasetInfo labelProviderDataset;
    @JsonIgnore private transient ModelJobRequest modelRequest = new ModelJobRequest();

    public ModelJobParam() {}

    public void check() throws Exception {
        if (dataSetList == null || dataSetList.isEmpty()) {
            throw new WeDPRException(
                    "Invalid model job param, must define the dataSet information!");
        }
        if (this.jobType == null) {
            throw new WeDPRException("Invalid model job param, must define the job type!");
        }
        modelRequest.setJobID(jobID);
        for (DatasetInfo datasetInfo : dataSetList) {
            datasetInfo.setDatasetIDList(datasetIDList);
            datasetInfo.check();
            if (datasetInfo.getReceiveResult()) {
                modelRequest
                        .getResultReceiverIDList()
                        .add(datasetInfo.getDataset().getOwnerAgency());
            }
        }
        parseLabelProviderInfo();
        parseParticipants();
        // set the model params for all the jobs
        if (modelSetting == null) {
            throw new WeDPRException(
                    "The job with type " + jobType.getType() + " must define settings!");
        }
        this.modelRequest.setModelParam(modelSetting);
        if (modelSetting != null) {
            BaseModelSetting baseModelSetting =
                    ObjectMapperFactory.getObjectMapper()
                            .convertValue(modelSetting, BaseModelSetting.class);
            this.modelRequest.setBaseModelSetting(baseModelSetting);
        }

        // set the model params for the predicting job
        if (jobType.predictJob()) {
            this.modelRequest.setModelPredictAlgorithm(
                    SettingTemplateDO.deserialize(this.modelPredictAlgorithm).getSetting());
        }
    }

    public void parseLabelProviderInfo() throws Exception {

        String selfAgency = WeDPRCommonConfig.getAgency();

        for (DatasetInfo datasetInfo : dataSetList) {
            if (datasetInfo.getDataset().getOwnerAgency().compareToIgnoreCase(selfAgency) == 0) {
                this.selfDataset = datasetInfo;
                if (this.labelProviderDataset != null) {
                    break;
                }
            }
            if (datasetInfo.getLabelProvider()) {
                this.labelProviderDataset = datasetInfo;
                if (this.selfDataset != null) {
                    break;
                }
            }
        }
        if (this.selfDataset == null) {
            throw new WeDPRException(
                    "Invalid model job param, the dataSet for participant agency "
                            + WeDPRCommonConfig.getAgency()
                            + " not set!");
        }

        this.modelRequest.setDatasetPath(this.selfDataset.getDataset().getPath());
        if (this.labelProviderDataset == null) {
            throw new WeDPRException("Invalid model job param, Must define the labelProvider");
        }
        // set the label provider information
        modelRequest.setIsLabelProvider(
                (this.labelProviderDataset
                                .getDataset()
                                .getOwnerAgency()
                                .compareToIgnoreCase(WeDPRCommonConfig.getAgency())
                        == 0));
    }

    // set the participants information
    public void parseParticipants() {
        // set the active party
        this.modelRequest
                .getParticipantIDList()
                .add(this.labelProviderDataset.getDataset().getOwnerAgency());
        // set the passive parties
        for (DatasetInfo datasetInfo : dataSetList) {
            if (datasetInfo
                            .getDataset()
                            .getOwnerAgency()
                            .compareToIgnoreCase(
                                    this.labelProviderDataset.getDataset().getOwnerAgency())
                    == 0) {
                continue;
            }
            this.modelRequest.getParticipantIDList().add(datasetInfo.getDataset().getOwnerAgency());
        }
    }

    public PreprocessingRequest toPreprocessingRequest(FileMetaBuilder fileMetaBuilder)
            throws Exception {
        parseIDFilePath(fileMetaBuilder);
        if (this.jobType.predictJob()) {
            return new PreprocessingRequest(modelRequest, AlgorithmType.WEDPR_PREDICT);
        }
        if (this.jobType.trainJob()) {
            return new PreprocessingRequest(modelRequest, AlgorithmType.WEDPR_TRAIN);
        }
        throw new WeDPRException(
                "Job " + jobType.getType() + "can't be converted to preprocessing request!");
    }

    public FeatureEngineeringRequest toFeatureEngineerRequest() {
        if (!(modelRequest.getBaseModelSetting()).getUseIv()) {
            return null;
        }
        return new FeatureEngineeringRequest(modelRequest);
    }

    public ModelJobRequest toMultiPartyMlJobRequest() {
        if (!JobType.isMultiPartyMlJob(this.jobType.getType())) {
            return null;
        }
        return new ModelJobRequest(modelRequest, this.jobType);
    }

    public void parseIDFilePath(FileMetaBuilder fileMetaBuilder) {
        FileMeta output =
                PSIJobParam.getDefaultPSIOutputPath(
                        fileMetaBuilder, selfDataset.getDataset(), jobID);
        modelRequest.setIdFilePath(output.getPath());
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

    public boolean usePSI() {
        if (this.modelRequest == null || this.modelRequest.getBaseModelSetting() == null) {
            return false;
        }
        return this.modelRequest.getBaseModelSetting().getUsePsi();
    }

    public String serialize() throws Exception {
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
    }

    public static ModelJobParam deserialize(String data) throws Exception {
        return ObjectMapperFactory.getObjectMapper().readValue(data, ModelJobParam.class);
    }
}
