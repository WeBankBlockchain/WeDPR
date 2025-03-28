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
import com.webank.wedpr.components.uuid.generator.WeDPRUuidGenerator;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class PirDatasetConstructorImpl implements PirDatasetConstructor {
    private static final Logger logger = LoggerFactory.getLogger(PirDatasetConstructor.class);

    private final DatasetMapper datasetMapper;
    private final FileStorageInterface fileStorageInterface;
    private final JdbcTemplate jdbcTemplate;
    private final String dbName;

    public PirDatasetConstructorImpl(
            DatasetMapper datasetMapper,
            FileStorageInterface fileStorageInterface,
            JdbcTemplate jdbcTemplate)
            throws SQLException {
        this.datasetMapper = datasetMapper;
        this.fileStorageInterface = fileStorageInterface;
        this.jdbcTemplate = jdbcTemplate;
        this.dbName = this.jdbcTemplate.getDataSource().getConnection().getCatalog();
        logger.info("Current database name: {}", this.dbName);
    }

    @Override
    public void construct(PirServiceSetting serviceSetting) throws Exception {
        Dataset dataset = null;
        String tableId = null;
        try {
            String datasetID = serviceSetting.getDatasetId();
            tableId =
                    com.webank.wedpr.components.task.plugin.pir.utils.Constant.datasetId2tableId(
                            datasetID);
            if (tableExists(tableId)) {
                logger.info(
                        "The dataset {} has already been constructed into {}", datasetID, tableId);
                return;
            }
            dataset = this.datasetMapper.getDatasetByDatasetId(datasetID, false);
            // create table
            DataSourceType dataSourceType = DataSourceType.fromStr(dataset.getDataSourceType());
            long startT = System.currentTimeMillis();
            logger.info(
                    "Load pir service, dataset: {}, type: {}",
                    dataset.getDatasetId(),
                    dataSourceType.name());

            constructFromCSV(tableId, dataset, serviceSetting.getIdField());
            logger.info(
                    "Load pir success, dataset: {}, type: {}, timecost: {}ms",
                    dataset.getDatasetId(),
                    dataSourceType.name(),
                    System.currentTimeMillis() - startT);
        } catch (Exception e) {
            logger.warn(
                    "Publish pir service failed, dataset: {}, e: ",
                    (dataset == null ? "empty" : dataset.getDatasetId()),
                    e);
            if (StringUtils.isNotBlank(tableId)) {
                logger.info("Revert the created table: {}", tableId);
                this.jdbcTemplate.execute("drop table if exists " + tableId);
            }
            throw e;
        }
    }

    private boolean tableExists(String tableName) {
        String query =
                String.format(
                        "select count(*) "
                                + "from information_schema.tables "
                                + "where table_name = ? and table_schema = '%s'",
                        this.dbName);
        Integer result = jdbcTemplate.queryForObject(query, Integer.class, tableName);
        return result != null && result > 0;
    }

    private Pair<List<String>, Integer> createPirTableForDataset(
            String tableId, String idField, String[] datasetFields) {

        logger.info("Create table {}", tableId);
        // all the field + id_hash field
        String[] fieldsWithType = new String[datasetFields.length + 2];
        List<String> tableFields = new ArrayList<>();
        int idFieldIndex = 0;
        for (int i = 0; i < datasetFields.length; i++) {
            // the idField
            if (idField.equalsIgnoreCase(datasetFields[i])) {
                fieldsWithType[i] = idField + " VARCHAR(255)";
                tableFields.add(idField);
                idFieldIndex = i;
            } else {
                fieldsWithType[i] = datasetFields[i] + " TEXT";
                tableFields.add(datasetFields[i]);
            }
        }
        // add the id fields(uuid)
        fieldsWithType[datasetFields.length] = Constant.PIR_ID_FIELD_NAME + " VARCHAR(64)";
        tableFields.add(Constant.PIR_ID_FIELD_NAME);
        // add the id_hash field at the last
        fieldsWithType[datasetFields.length + 1] = Constant.PIR_ID_HASH_FIELD_NAME + " VARCHAR(64)";
        tableFields.add(Constant.PIR_ID_HASH_FIELD_NAME);

        String sql =
                String.format(
                        "CREATE TABLE %s ( %s , PRIMARY KEY (`%s`) USING BTREE, index id_index(`%s`(128)) )",
                        tableId,
                        String.join(",", fieldsWithType),
                        Constant.PIR_ID_FIELD_NAME,
                        idField);
        logger.info("createPirTableForDataset, execute sql: {}", sql);
        this.jdbcTemplate.execute(sql);
        return new ImmutablePair<>(tableFields, idFieldIndex);
    }

    private void constructFromCSV(String tableId, Dataset dataset, String idField)
            throws Exception {
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
        if (datasetFieldsList.contains(Constant.PIR_ID_HASH_FIELD_NAME)) {
            throw new WeDPRException("Conflict with sys field " + Constant.PIR_ID_HASH_FIELD_NAME);
        }
        if (datasetFieldsList.contains(Constant.PIR_ID_FIELD_NAME)) {
            throw new WeDPRException("Conflict with sys field " + Constant.PIR_ID_FIELD_NAME);
        }
        Pair<List<String>, Integer> tableInfo =
                createPirTableForDataset(tableId, idField, datasetFields);
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
                        // the id field
                        rowContent.add(WeDPRUuidGenerator.generateID());
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
                        jdbcTemplate.execute(sql);
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
