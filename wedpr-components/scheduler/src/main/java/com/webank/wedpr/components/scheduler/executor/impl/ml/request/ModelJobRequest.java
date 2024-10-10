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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.webank.wedpr.core.utils.BaseRequest;
import com.webank.wedpr.core.utils.Common;
import com.webank.wedpr.core.utils.ObjectMapperFactory;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

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
        this.setLabelProvider(modelJobRequest.getLabelProvider());
    }

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getDatasetPath() {
        return datasetPath;
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

    public String getAlgorithmType() {
        return algorithmType;
    }

    public void setAlgorithmType(String algorithmType) {
        this.algorithmType = algorithmType;
    }

    public List<String> getResultReceiverIDList() {
        return resultReceiverIDList;
    }

    public void setResultReceiverIDList(List<String> resultReceiverIDList) {
        this.resultReceiverIDList = resultReceiverIDList;
    }

    public List<String> getParticipantIDList() {
        return participantIDList;
    }

    public void setParticipantIDList(List<String> participantIDList) {
        this.participantIDList = participantIDList;
    }

    public String getDatasetID() {
        return datasetID;
    }

    public void setDatasetID(String datasetID) {
        this.datasetID = datasetID;
    }

    public Object getModelParam() {
        return modelParam;
    }

    public void setModelParam(Object modelParam) {
        this.modelParam = modelParam;
    }

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        if (StringUtils.isBlank(taskID)) {
            return;
        }
        this.taskID = taskID;
    }

    public String getModelPredictAlgorithm() {
        return modelPredictAlgorithm;
    }

    public void setModelPredictAlgorithm(String modelPredictAlgorithm) {
        this.modelPredictAlgorithm = modelPredictAlgorithm;
    }

    public Boolean getLabelProvider() {
        return isLabelProvider;
    }

    public void setLabelProvider(Boolean labelProvider) {
        isLabelProvider = labelProvider;
    }

    public String getIdFilePath() {
        return idFilePath;
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
