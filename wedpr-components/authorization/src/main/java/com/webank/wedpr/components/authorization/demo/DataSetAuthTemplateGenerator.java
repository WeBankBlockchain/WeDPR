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

package com.webank.wedpr.components.authorization.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.webank.wedpr.components.authorization.model.FormTemplateSetting;
import java.util.ArrayList;
import java.util.List;

public class DataSetAuthTemplateGenerator {
    public static void main(String[] args) throws JsonProcessingException {
        String templateName = "wedpr_data_auth";
        if (args.length >= 1) {
            templateName = args[0];
        }
        System.out.println(
                "====== DataSetAuthTemplateGenerator, templateName: " + templateName + " ======");
        FormTemplateSetting setting = new FormTemplateSetting();
        setting.setName(templateName);
        List<FormTemplateSetting.FormColumnDefinition> columns = new ArrayList<>();
        // the datasetID column
        FormTemplateSetting.FormColumnDefinition dataIDColumn =
                new FormTemplateSetting.FormColumnDefinition();
        dataIDColumn.setKey("datasetID");
        dataIDColumn.setTitle("数据集ID");
        dataIDColumn.setType(FormTemplateSetting.ColumnType.Number.getName());
        dataIDColumn.setRequired(Boolean.TRUE);
        dataIDColumn.setVisibility(Boolean.TRUE);
        columns.add(dataIDColumn);
        // the datasetName column
        FormTemplateSetting.FormColumnDefinition dataSetNameColumn =
                new FormTemplateSetting.FormColumnDefinition();
        dataSetNameColumn.setKey("dataSetName");
        dataSetNameColumn.setTitle("数据集名称");
        dataSetNameColumn.setRequired(Boolean.TRUE);
        dataSetNameColumn.setVisibility(Boolean.TRUE);
        columns.add(dataSetNameColumn);
        // the datasetOwner column
        FormTemplateSetting.FormColumnDefinition datasetOwnerColumn =
                new FormTemplateSetting.FormColumnDefinition();
        datasetOwnerColumn.setKey("datasetOwner");
        datasetOwnerColumn.setTitle("数据集属主");
        datasetOwnerColumn.setRequired(Boolean.TRUE);
        columns.add(datasetOwnerColumn);
        // the datasetOwnerAgency column
        FormTemplateSetting.FormColumnDefinition datasetOwnerAgencyColumn =
                new FormTemplateSetting.FormColumnDefinition();
        datasetOwnerAgencyColumn.setKey("datasetOwnerAgency");
        datasetOwnerAgencyColumn.setTitle("数据集所属机构");
        datasetOwnerAgencyColumn.setRequired(Boolean.TRUE);
        columns.add(datasetOwnerAgencyColumn);
        // the auth-time column
        FormTemplateSetting.FormColumnDefinition authTimeColumn =
                new FormTemplateSetting.FormColumnDefinition();
        authTimeColumn.setKey("authTime");
        authTimeColumn.setTitle("授权时间");
        authTimeColumn.setRequired(Boolean.TRUE);
        authTimeColumn.setType(FormTemplateSetting.ColumnType.Date.getName());
        columns.add(authTimeColumn);
        setting.setColumns(columns);
        System.out.println("DataSetAuthTemplateSetting: ");
        System.out.println(setting.serialize());
        System.out.println(
                "====== DataSetAuthTemplateGenerator success, templateName: "
                        + templateName
                        + " ======");
    }
}
