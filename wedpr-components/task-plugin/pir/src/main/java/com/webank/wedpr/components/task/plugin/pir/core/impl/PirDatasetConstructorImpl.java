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

package com.webank.wedpr.components.task.plugin.pir.core.impl;

import com.webank.wedpr.common.utils.CSVFileParser;
import com.webank.wedpr.common.utils.Common;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.crypto.CryptoToolkitFactory;
import com.webank.wedpr.components.db.mapper.dataset.dao.Dataset;
import com.webank.wedpr.components.db.mapper.dataset.datasource.DataSourceType;
import com.webank.wedpr.components.db.mapper.dataset.mapper.DatasetMapper;
import com.webank.wedpr.components.db.mapper.service.publish.model.PirServiceSetting;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import com.webank.wedpr.components.storage.api.StoragePath;
import com.webank.wedpr.components.storage.builder.StoragePathBuilder;
import com.webank.wedpr.components.task.plugin.pir.config.PirServiceConfig;
import com.webank.wedpr.components.task.plugin.pir.core.PirDatasetConstructor;
import com.webank.wedpr.components.task.plugin.pir.dao.NativeSQLMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PirDatasetConstructorImpl implements PirDatasetConstructor {
    private static final Logger logger = LoggerFactory.getLogger(PirDatasetConstructor.class);

    private final DatasetMapper datasetMapper;
    private final FileStorageInterface fileStorageInterface;
    private final NativeSQLMapper nativeSQLMapper;

    public PirDatasetConstructorImpl(
            DatasetMapper datasetMapper,
            FileStorageInterface fileStorageInterface,
            NativeSQLMapper nativeSQLMapper) {
        this.datasetMapper = datasetMapper;
        this.fileStorageInterface = fileStorageInterface;
        this.nativeSQLMapper = nativeSQLMapper;
    }

    @Override
    public void construct(PirServiceSetting serviceSetting) throws Exception {
        List<String> allTables = this.nativeSQLMapper.showAllTables();
        String datasetID = serviceSetting.getDatasetId();
        String tableId =
                com.webank.wedpr.components.task.plugin.pir.utils.Constant.datasetId2tableId(
                        datasetID);
        if (allTables.contains(tableId)) {
            logger.info("The dataset {} has already been constructed into {}", datasetID, tableId);
            return;
        }
        Dataset dataset = this.datasetMapper.getDatasetByDatasetId(datasetID, false);
        DataSourceType dataSourceType = DataSourceType.fromStr(dataset.getDataSourceType());
        if (dataSourceType != DataSourceType.CSV && dataSourceType != DataSourceType.EXCEL) {
            throw new WeDPRException("PIR only support CSV and excel DataSources now!");
        }
        logger.info("constructFromCSV, dataset: {}", dataset.getDatasetId());
        constructFromCSV(dataset, serviceSetting.getIdField());
        logger.info("constructFromCSV success, dataset: {}", dataset.getDatasetId());
    }

    private Pair<List<String>, Integer> createTable(
            String tableId, String idField, String[] datasetFields) {

        logger.info("Create table {}", tableId);
        // all the field + id_hash field
        String[] fieldsWithType = new String[datasetFields.length + 1];
        List<String> tableFields = new ArrayList<>();
        int idFieldIndex = 0;
        for (int i = 0; i < datasetFields.length; i++) {
            // the idField
            if (idField.equalsIgnoreCase(datasetFields[i])) {
                fieldsWithType[i] = Constant.PIR_ID_FIELD_NAME + " VARCHAR(255)";
                tableFields.add(Constant.PIR_ID_FIELD_NAME);
                idFieldIndex = i;
            } else {
                fieldsWithType[i] = datasetFields[i] + " TEXT";
                tableFields.add(datasetFields[i]);
            }
        }
        // add the id_hash field at the last
        fieldsWithType[datasetFields.length] = Constant.PIR_ID_HASH_FIELD_NAME + " VARCHAR(64)";
        tableFields.add(Constant.PIR_ID_HASH_FIELD_NAME);

        String sql =
                String.format(
                        "CREATE TABLE %s ( %s , PRIMARY KEY (`%s`) USING BTREE, index id_index(`%s`(128)) )",
                        tableId,
                        String.join(",", fieldsWithType),
                        Constant.PIR_ID_HASH_FIELD_NAME,
                        Constant.PIR_ID_FIELD_NAME);
        logger.info("createTable, execute sql: {}", sql);
        this.nativeSQLMapper.executeNativeUpdateSql(sql);
        return new ImmutablePair<>(tableFields, idFieldIndex);
    }

    private void constructFromCSV(Dataset dataset, String idField) throws Exception {
        StoragePath storagePath =
                StoragePathBuilder.getInstance(
                        dataset.getDatasetStorageType(), dataset.getDatasetStoragePath());
        String localFilePath =
                Common.joinPath(PirServiceConfig.getPirCacheDir(), dataset.getDatasetId());
        this.fileStorageInterface.download(storagePath, localFilePath);
        logger.info(
                "Download dataset {} success, localFilePath: {}",
                dataset.getDatasetId(),
                localFilePath);
        String[] datasetFields =
                Arrays.stream(dataset.getDatasetFields().trim().split(","))
                        .map(String::trim)
                        .toArray(String[]::new);
        List<String> datasetFieldsList = Arrays.asList(datasetFields);
        if (datasetFieldsList.contains(Constant.PIR_ID_FIELD_NAME)) {
            throw new WeDPRException("Conflict with sys field " + Constant.PIR_ID_FIELD_NAME);
        }
        if (datasetFieldsList.contains(Constant.PIR_ID_HASH_FIELD_NAME)) {
            throw new WeDPRException("Conflict with sys field " + Constant.PIR_ID_HASH_FIELD_NAME);
        }
        // create table
        String tableId =
                com.webank.wedpr.components.task.plugin.pir.utils.Constant.datasetId2tableId(
                        dataset.getDatasetId());
        Pair<List<String>, Integer> tableInfo = createTable(tableId, idField, datasetFields);
        Integer idFieldIndex = tableInfo.getRight();

        long startTime = System.currentTimeMillis();
        final Long[] publishedRecorders = {0L};
        final Long reportRecorders = 10000L;
        CSVFileParser.processCsvContent(
                datasetFields,
                localFilePath,
                new CSVFileParser.RowContentHandler() {
                    @Override
                    public void handle(List<String> rowContent) throws Exception {
                        StringBuilder sb = new StringBuilder();
                        // add hash for the idField
                        rowContent.add(CryptoToolkitFactory.hash(rowContent.get(idFieldIndex)));
                        sb.append("(")
                                .append(Common.joinAndAddDoubleQuotes(rowContent))
                                .append(")");
                        // insert the row-content into sql
                        String sql =
                                String.format(
                                        "INSERT INTO %s (%s) VALUES %s ",
                                        tableId, String.join(",", tableInfo.getLeft()), sb);
                        publishedRecorders[0] += 1;
                        if (publishedRecorders[0] % reportRecorders == 0) {
                            logger.info(
                                    "table: {}, dataset: {} publishing, publishedRecorders: {}, timecost: {}ms",
                                    tableId,
                                    dataset.getDatasetId(),
                                    publishedRecorders[0],
                                    (System.currentTimeMillis() - startTime));
                        }
                        nativeSQLMapper.executeNativeUpdateSql(sql);
                    }
                });
        logger.info(
                "Publish pir success, table: {}, dataset: {}, publishedRecorders: {}, timecost: {}ms",
                tableId,
                dataset.getDatasetId(),
                publishedRecorders[0],
                (System.currentTimeMillis() - startTime));
    }
}
