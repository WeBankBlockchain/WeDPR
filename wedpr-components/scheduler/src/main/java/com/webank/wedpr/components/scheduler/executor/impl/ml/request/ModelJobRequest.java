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

package com.webank.wedpr.components.scheduler.executor.impl.ml.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.webank.wedpr.common.protocol.JobType;
import com.webank.wedpr.common.utils.BaseRequest;
import com.webank.wedpr.common.utils.Common;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.components.scheduler.executor.impl.ml.model.BaseModelSetting;
import com.webank.wedpr.components.scheduler.executor.impl.model.AlgorithmType;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModelJobRequest implements BaseRequest {
    @JsonProperty("psi_result_path")
    protected String idFilePath;

    @JsonProperty("job_id")
    protected String jobID;

    @JsonProperty("task_id")
    protected String taskID;

    @JsonProperty("is_label_holder")
    protected Boolean isLabelProvider;

    @JsonProperty("task_type")
    protected String taskType;

    @JsonProperty("dataset_path")
    protected String datasetPath;

    @JsonProperty("dataset_id")
    protected String datasetID;

    @JsonProperty("algorithm_type")
    protected String algorithmType;

    @JsonProperty("result_receiver_id_list")
    protected List<String> resultReceiverIDList = new ArrayList<>();

    @JsonProperty("participant_id_list")
    protected List<String> participantIDList = new ArrayList<>();

    @JsonProperty("model_dict")
    protected Object modelParam;

    // the baseModelSetting used to determine need to run psi/featureEngineer or not
    @JsonIgnore protected BaseModelSetting baseModelSetting;

    @JsonProperty("model_predict_algorithm")
    protected String modelPredictAlgorithm;

    public ModelJobRequest() {}

    public ModelJobRequest(ModelJobRequest modelJobRequest) {
        this.setIdFilePath(modelJobRequest.getIdFilePath());
        this.setJobID(modelJobRequest.getJobID());
        this.setAlgorithmType(modelJobRequest.getAlgorithmType());
        this.setDatasetPath(modelJobRequest.getDatasetPath());
        this.setDatasetID(modelJobRequest.getDatasetID());
        this.setModelPredictAlgorithm(modelJobRequest.getModelPredictAlgorithm());
        this.setModelParam(modelJobRequest.getModelParam());
        this.setParticipantIDList(modelJobRequest.getParticipantIDList());
        this.setResultReceiverIDList(modelJobRequest.getResultReceiverIDList());
        this.setTaskID(modelJobRequest.getTaskID());
        this.setTaskType(modelJobRequest.getTaskType());
        this.setIsLabelProvider(modelJobRequest.getIsLabelProvider());
    }

    public ModelJobRequest(ModelJobRequest modelJobRequest, JobType jobType) {
        this(modelJobRequest);
        this.setTaskType(jobType.getType());
        if (jobType.trainJob()) {
            this.algorithmType = AlgorithmType.WEDPR_TRAIN.getType();
        } else {
            this.algorithmType = AlgorithmType.WEDPR_PREDICT.getType();
        }
    }

    public void setDatasetPath(String datasetPath) {
        this.datasetPath = datasetPath;

        if (this.datasetID != null && !datasetID.isEmpty()) {
            return;
        }

        if (StringUtils.isBlank(this.datasetPath)) {
            return;
        }
        this.datasetID = Common.getFileName(datasetPath);
    }

    public void setTaskID(String taskID) {
        if (StringUtils.isBlank(taskID)) {
            return;
        }
        this.taskID = taskID;
    }

    public void setIdFilePath(String idFilePath) {
        if (StringUtils.isBlank(idFilePath)) {
            return;
        }
        this.idFilePath = idFilePath;
    }

    @Override
    public String serialize() throws Exception {
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
    }
}
