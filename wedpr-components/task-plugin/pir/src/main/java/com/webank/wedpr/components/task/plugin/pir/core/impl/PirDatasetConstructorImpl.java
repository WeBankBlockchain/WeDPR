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

import com.webank.wedpr.components.db.mapper.dataset.dao.Dataset;
import com.webank.wedpr.components.db.mapper.dataset.datasource.DataSourceType;
import com.webank.wedpr.components.db.mapper.dataset.mapper.DatasetMapper;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import com.webank.wedpr.components.storage.api.StoragePath;
import com.webank.wedpr.components.storage.builder.StoragePathBuilder;
import com.webank.wedpr.components.task.plugin.pir.config.PirServiceConfig;
import com.webank.wedpr.components.task.plugin.pir.core.PirDatasetConstructor;
import com.webank.wedpr.components.task.plugin.pir.dao.NativeSQLMapper;
import com.webank.wedpr.components.task.plugin.pir.utils.Constant;
import com.webank.wedpr.core.utils.CSVFileParser;
import com.webank.wedpr.core.utils.Common;
import com.webank.wedpr.core.utils.WeDPRException;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PirDatasetConstructorImpl implements PirDatasetConstructor {
    private static Logger logger = LoggerFactory.getLogger(PirDatasetConstructor.class);

    private final DatasetMapper datasetMapper;
    private final StoragePathBuilder storagePathBuilder;
    private final FileStorageInterface fileStorageInterface;
    private final NativeSQLMapper nativeSQLMapper;

    public PirDatasetConstructorImpl(
            DatasetMapper datasetMapper,
            FileStorageInterface fileStorageInterface,
            StoragePathBuilder storagePathBuilder,
            NativeSQLMapper nativeSQLMapper) {
        this.datasetMapper = datasetMapper;
        this.fileStorageInterface = fileStorageInterface;
        this.storagePathBuilder = storagePathBuilder;
        this.nativeSQLMapper = nativeSQLMapper;
    }

    @Override
    public void construct(String datasetID) throws Exception {
        List<String> allTables = this.nativeSQLMapper.showAllTables();
        String tableId = Constant.datasetId2tableId(datasetID);
        if (allTables.contains(tableId)) {
            logger.info("The dataset {} has already been constructed into {}", datasetID, tableId);
            return;
        }
        Dataset dataset = this.datasetMapper.getDatasetByDatasetId(datasetID, false);
        DataSourceType dataSourceType = DataSourceType.fromStr(dataset.getDatasetStorageType());
        if (dataSourceType != DataSourceType.CSV) {
            throw new WeDPRException("PIR only support CSV DataSources now!");
        }
        constructFromCSV(dataset);
    }

    private void constructFromCSV(Dataset dataset) throws Exception {
        StoragePath storagePath =
                StoragePathBuilder.getInstance(
                        dataset.getDatasetStorageType(), dataset.getDatasetStoragePath());
        String localFilePath =
                Common.joinPath(PirServiceConfig.getPirCacheDir(), dataset.getDatasetId());
        this.fileStorageInterface.download(storagePath, localFilePath);
        String[] datasetFields =
                Arrays.stream(dataset.getDatasetFields().trim().split(","))
                        .map(String::trim)
                        .toArray(String[]::new);

        List<List<String>> sqlValues =
                CSVFileParser.processCsv2SqlMap(datasetFields, localFilePath);
        if (sqlValues.size() == 0) {
            logger.info(
                    "constructFromCSV with empty dataset, datasetID: {}, datasetPath: {}",
                    dataset.getDatasetId(),
                    localFilePath);
            return;
        }
        String tableId = Constant.datasetId2tableId(dataset.getDatasetId());
        String createSqlFormat = "CREATE TABLE %s ( %s , PRIMARY KEY (`id`) USING BTREE )";

        String[] fieldsWithType = new String[datasetFields.length];
        for (int i = 0; i < datasetFields.length; i++) {
            if ("id".equalsIgnoreCase(datasetFields[i])) {
                fieldsWithType[i] = datasetFields[i] + " VARCHAR(64)";
            } else {
                fieldsWithType[i] = datasetFields[i] + " TEXT";
            }
        }
        String sql = String.format(createSqlFormat, tableId, String.join(",", fieldsWithType));
        logger.info("constructFromCSV, execute sql: {}", sql);
        this.nativeSQLMapper.executeNativeUpdateSql(sql);

        StringBuilder sb = new StringBuilder();
        for (List<String> values : sqlValues) {
            sb.append("(").append(String.join(",", values)).append("), ");
        }
        String insertValues = sb.toString();
        insertValues = insertValues.substring(0, insertValues.length() - 2);

        String insertSqlFormat = "INSERT INTO %s (%s) VALUES %s ";
        sql =
                String.format(
                        insertSqlFormat, tableId, String.join(",", datasetFields), insertValues);
        this.nativeSQLMapper.executeNativeUpdateSql(sql);
    }
}
