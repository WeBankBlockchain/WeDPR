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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.webank.wedpr.core.utils.ObjectMapperFactory;

@JsonIgnoreProperties(ignoreUnknown = true)
public class XGBModelSetting {
    @JsonProperty("use_psi")
    private Boolean usePsi = false;

    private Boolean fillna = false;
    private Boolean normalized = false;
    private Boolean standardized = false;
    private String categorical = "";

    @JsonProperty("psi_select_col")
    private Integer psiSelectCol = 0;

    @JsonProperty("psi_select_base")
    private Integer psiSelectBase = 0;

    @JsonProperty("psi_select_thresh")
    private String psiSelectThresh = "0.3";

    @JsonProperty("psi_select_bins")
    private Integer psiSelectBins = 4;

    @JsonProperty("corr_select")
    private String corrSelect = "0.99";

    @JsonProperty("use_iv")
    private Boolean useIv = false;

    @JsonProperty("group_num")
    private Integer groupNum = 10;

    @JsonProperty("iv_thresh")
    private String ivThresh = "0.01";

    @JsonProperty("use_goss")
    private Boolean useGoss = false;

    @JsonProperty("test_dataset_percentage")
    private String testDatasetPercentage = "0.3";

    @JsonProperty("learning_rate")
    private String learningRate = "0.1";

    @JsonProperty("num_trees")
    private Integer numTrees = 50;

    @JsonProperty("max_depth")
    private Integer maxDepth = 4;

    @JsonProperty("max_bin")
    private Integer maxBin = 4;

    private Boolean silent = false;
    private String subsample = "1";

    @JsonProperty("colsample_bytree")
    private String colsampleBytree = "1";

    @JsonProperty("colsample_bylevel")
    private String colsampleBylevel = "1";

    @JsonProperty("reg_alpha")
    private String regAlpha = "0";

    @JsonProperty("reg_lambda")
    private String regLambda = "1";

    @JsonProperty("gamma")
    private String gamma = "0";

    @JsonProperty("min_child_weight")
    private Integer minChildWeight = 0;

    @JsonProperty("min_child_samples")
    private Integer minChildSamples = 10;

    @JsonProperty("seed")
    private Integer seed = 2024;

    @JsonProperty("early_stopping_rounds")
    private Integer earlyStoppingRounds = 0;

    @JsonProperty("eval_metric")
    private String evalMetric = "auc";

    @JsonProperty("verbose_eval")
    private Integer verboseEval = 1;

    @JsonProperty("eval_set_column")
    private String evalSetColumn = "";

    @JsonProperty("train_set_value")
    private String trainSetValue = "";

    @JsonProperty("eval_set_value")
    private String evalSetValue = "";

    @JsonProperty("train_features")
    private String trainFeatures = "";

    public Boolean getUsePsi() {
        return usePsi;
    }

    public void setUsePsi(Boolean usePsi) {
        this.usePsi = usePsi;
    }

    public Boolean getFillna() {
        return fillna;
    }

    public void setFillna(Boolean fillna) {
        this.fillna = fillna;
    }

    public Boolean getNormalized() {
        return normalized;
    }

    public void setNormalized(Boolean normalized) {
        this.normalized = normalized;
    }

    public Boolean getStandardized() {
        return standardized;
    }

    public void setStandardized(Boolean standardized) {
        this.standardized = standardized;
    }

    public String getCategorical() {
        return categorical;
    }

    public void setCategorical(String categorical) {
        this.categorical = categorical;
    }

    public Integer getPsiSelectCol() {
        return psiSelectCol;
    }

    public void setPsiSelectCol(Integer psiSelectCol) {
        this.psiSelectCol = psiSelectCol;
    }

    public Integer getPsiSelectBase() {
        return psiSelectBase;
    }

    public void setPsiSelectBase(Integer psiSelectBase) {
        this.psiSelectBase = psiSelectBase;
    }

    public String getPsiSelectThresh() {
        return psiSelectThresh;
    }

    public void setPsiSelectThresh(String psiSelectThresh) {
        this.psiSelectThresh = psiSelectThresh;
    }

    public Integer getPsiSelectBins() {
        return psiSelectBins;
    }

    public void setPsiSelectBins(Integer psiSelectBins) {
        this.psiSelectBins = psiSelectBins;
    }

    public String getCorrSelect() {
        return corrSelect;
    }

    public void setCorrSelect(String corrSelect) {
        this.corrSelect = corrSelect;
    }

    public Boolean getUseIv() {
        return useIv;
    }

    public void setUseIv(Boolean useIv) {
        this.useIv = useIv;
    }

    public Integer getGroupNum() {
        return groupNum;
    }

    public void setGroupNum(Integer groupNum) {
        this.groupNum = groupNum;
    }

    public String getIvThresh() {
        return ivThresh;
    }

    public void setIvThresh(String ivThresh) {
        this.ivThresh = ivThresh;
    }

    public Boolean getUseGoss() {
        return useGoss;
    }

    public void setUseGoss(Boolean useGoss) {
        this.useGoss = useGoss;
    }

    public String getTestDatasetPercentage() {
        return testDatasetPercentage;
    }

    public void setTestDatasetPercentage(String testDatasetPercentage) {
        this.testDatasetPercentage = testDatasetPercentage;
    }

    public String getLearningRate() {
        return learningRate;
    }

    public void setLearningRate(String learningRate) {
        this.learningRate = learningRate;
    }

    public Integer getNumTrees() {
        return numTrees;
    }

    public void setNumTrees(Integer numTrees) {
        this.numTrees = numTrees;
    }

    public Integer getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(Integer maxDepth) {
        this.maxDepth = maxDepth;
    }

    public Integer getMaxBin() {
        return maxBin;
    }

    public void setMaxBin(Integer maxBin) {
        this.maxBin = maxBin;
    }

    public Boolean getSilent() {
        return silent;
    }

    public void setSilent(Boolean silent) {
        this.silent = silent;
    }

    public String getSubsample() {
        return subsample;
    }

    public void setSubsample(String subsample) {
        this.subsample = subsample;
    }

    public String getColsampleBytree() {
        return colsampleBytree;
    }

    public void setColsampleBytree(String colsampleBytree) {
        this.colsampleBytree = colsampleBytree;
    }

    public String getColsampleBylevel() {
        return colsampleBylevel;
    }

    public void setColsampleBylevel(String colsampleBylevel) {
        this.colsampleBylevel = colsampleBylevel;
    }

    public String getRegAlpha() {
        return regAlpha;
    }

    public void setRegAlpha(String regAlpha) {
        this.regAlpha = regAlpha;
    }

    public String getRegLambda() {
        return regLambda;
    }

    public void setRegLambda(String regLambda) {
        this.regLambda = regLambda;
    }

    public String getGamma() {
        return gamma;
    }

    public void setGamma(String gamma) {
        this.gamma = gamma;
    }

    public Integer getMinChildWeight() {
        return minChildWeight;
    }

    public void setMinChildWeight(Integer minChildWeight) {
        this.minChildWeight = minChildWeight;
    }

    public Integer getMinChildSamples() {
        return minChildSamples;
    }

    public void setMinChildSamples(Integer minChildSamples) {
        this.minChildSamples = minChildSamples;
    }

    public Integer getSeed() {
        return seed;
    }

    public void setSeed(Integer seed) {
        this.seed = seed;
    }

    public Integer getEarlyStoppingRounds() {
        return earlyStoppingRounds;
    }

    public void setEarlyStoppingRounds(Integer earlyStoppingRounds) {
        this.earlyStoppingRounds = earlyStoppingRounds;
    }

    public String getEvalMetric() {
        return evalMetric;
    }

    public void setEvalMetric(String evalMetric) {
        this.evalMetric = evalMetric;
    }

    public Integer getVerboseEval() {
        return verboseEval;
    }

    public void setVerboseEval(Integer verboseEval) {
        this.verboseEval = verboseEval;
    }

    public String getEvalSetColumn() {
        return evalSetColumn;
    }

    public void setEvalSetColumn(String evalSetColumn) {
        this.evalSetColumn = evalSetColumn;
    }

    public String getTrainSetValue() {
        return trainSetValue;
    }

    public void setTrainSetValue(String trainSetValue) {
        this.trainSetValue = trainSetValue;
    }

    public String getEvalSetValue() {
        return evalSetValue;
    }

    public void setEvalSetValue(String evalSetValue) {
        this.evalSetValue = evalSetValue;
    }

    public String getTrainFeatures() {
        return trainFeatures;
    }

    public void setTrainFeatures(String trainFeatures) {
        this.trainFeatures = trainFeatures;
    }

    public String serialize() throws JsonProcessingException {
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
    }
}
