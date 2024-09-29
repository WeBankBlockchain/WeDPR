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
package com.webank.wedpr.components.dataset.config;

import static com.webank.wedpr.components.dataset.service.ChunkUploadImpl.UPLOAD_CHUNK_FILE_NAME_PREFIX;

import com.webank.wedpr.components.db.mapper.dataset.common.DatasetConstant;
import com.webank.wedpr.core.utils.Common;
import java.io.File;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class DatasetConfig {
    @Value("${wedpr.dataset.largeFileDataDir}")
    String largeFileDataDir;

    @Value("${wedpr.dataset.debugMode}")
    boolean debugModel;

    @Value("${wedpr.dataset.debugMode.userTokenField}")
    String debugModelUserTokenField;

    @Value("${wedpr.dataset.maxBatchSize : 15}")
    int maxBatchSize;

    @Value("${wedpr.dataset.datasource.excel.defaultSheet : 0}")
    int excelDefaultSheet;

    @Value("${wedpr.datasource.datasetHash : SHA-256}")
    String datasetHash;

    @Value("${wedpr.storage.download.shardSize: 20971520}")
    int shardSize;

    // ${largeFileDataDir}/dataset/
    public String getDatasetBaseDir() {
        return String.format("%s/%s", largeFileDataDir, DatasetConstant.DATASET_LABEL);
    }

    // ${largeFileDataDir}/dataset/${datasetId}
    public String getDatasetDir(String datasetId) {
        String datasetBaseDir = getDatasetBaseDir();
        return String.format("%s/%s", datasetBaseDir, datasetId);
    }

    // ${largeFileDataDir}/dataset/chunks/
    public String getDatasetChunksBaseDir() {
        String datasetBaseDir = getDatasetBaseDir();
        return String.format("%s/%s", datasetBaseDir, UPLOAD_CHUNK_FILE_NAME_PREFIX);
    }

    // ${largeFileDataDir}/dataset/chunks/${datasetId}
    public String getDatasetChunksDir(String datasetId) {
        String datasetChunksBaseDir = getDatasetChunksBaseDir();
        return String.format("%s/%s", datasetChunksBaseDir, datasetId);
    }

    public String getDatasetStoragePath(String user, String datasetId, boolean dynamic) {
        if (dynamic) {
            // ${user}/dy/${currentTimeMillis}/${datasetId}
            long currentTimeMillis = System.currentTimeMillis();
            return user
                    + File.separator
                    + "dy"
                    + File.separator
                    + currentTimeMillis
                    + File.separator
                    + datasetId;
        } else {
            // ${user}/${datasetId}
            return Common.joinPath(user, datasetId);
        }
    }
}
