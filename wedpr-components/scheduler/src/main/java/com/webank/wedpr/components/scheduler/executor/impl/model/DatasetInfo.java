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

package com.webank.wedpr.components.scheduler.executor.impl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;

public class DatasetInfo {
    protected FileMeta dataset;
    protected FileMeta output;
    protected Boolean labelProvider = Boolean.FALSE;
    protected String labelField = Constant.DEFAULT_LABEL_FIELD;
    protected Boolean receiveResult = false;
    protected List<String> idFields = new ArrayList<>(Arrays.asList(Constant.DEFAULT_ID_FIELD));
    // TODO:
    protected Integer datasetRecordCount;

    @JsonIgnore protected List<String> datasetIDList;

    public List<String> getDatasetIDList() {
        return datasetIDList;
    }

    public void setDatasetIDList(List<String> datasetIDList) {
        this.datasetIDList = datasetIDList;
    }

    public FileMeta getDataset() {
        return dataset;
    }

    public void setDataset(FileMeta dataset) {
        this.dataset = dataset;
    }

    public Boolean getLabelProvider() {
        return labelProvider;
    }

    public void setLabelProvider(Boolean labelProvider) {
        this.labelProvider = labelProvider;
    }

    public String getLabelField() {
        return labelField;
    }

    public void setLabelField(String labelField) {
        if (StringUtils.isBlank(labelField)) {
            return;
        }
        this.labelField = labelField.trim();
    }

    @SneakyThrows(Exception.class)
    public void check() {
        if (this.dataset == null) {
            throw new WeDPRException("Invalid ML job param for no dataset defined!");
        }
        dataset.check(datasetIDList);
    }

    public List<String> getIdFields() {
        return idFields;
    }

    public Boolean getReceiveResult() {
        return receiveResult;
    }

    public void setReceiveResult(Boolean receiveResult) {
        if (receiveResult == null) {
            return;
        }
        this.receiveResult = receiveResult;
    }

    public void setIdFields(List<String> idFields) {
        if (idFields == null || idFields.isEmpty()) {
            return;
        }
        idFields.replaceAll(String::trim);
        this.idFields = idFields;
    }
}
