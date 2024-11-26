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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.webank.wedpr.common.protocol.StorageType;
import com.webank.wedpr.common.utils.Common;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.db.mapper.dataset.dao.Dataset;
import com.webank.wedpr.components.db.mapper.dataset.mapper.DatasetMapper;
import com.webank.wedpr.components.storage.api.StoragePath;
import com.webank.wedpr.components.storage.builder.StoragePathBuilder;
import java.util.List;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileMeta {
    public enum FileStorageType {
        HDFS(2),
        LocalFile(3);

        private final Integer type;

        FileStorageType(Integer type) {
            this.type = type;
        }

        public Integer getType() {
            return this.type;
        }
    }

    private String datasetID;
    @JsonIgnore protected Dataset dataset;
    protected Integer type;

    protected String storageTypeStr;
    @JsonIgnore protected transient StorageType storageType;

    // Note: path works only if the dataset id is not specified
    protected String path;
    protected String owner;
    protected String ownerAgency;
    protected long datasetRecordCount = 0;

    public FileMeta() {}

    public FileMeta(StorageType storageType, String path, String owner, String ownerAgency) {
        setStorageType(storageType);
        setPath(path);
        setOwner(owner);
        setOwnerAgency(ownerAgency);
    }

    public void obtainDatasetInfo(DatasetMapper datasetMapper) throws Exception {
        if (StringUtils.isBlank(datasetID)) {
            throw new WeDPRException("Invalid fileMeta, must define the datasetID");
        }
        this.dataset = datasetMapper.getDatasetByDatasetId(this.datasetID, false);
        // set the information
        setStorageTypeStr(this.dataset.getDatasetStorageType());
        setPath(this.dataset.getStoragePathMeta().getFilePath());
        setOwner(this.dataset.getOwnerUserName());
        setOwnerAgency(this.dataset.getOwnerAgencyName());
        setDatasetRecordCount(this.dataset.getDatasetRecordCount());
    }

    @SneakyThrows(Exception.class)
    public String serialize() {
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
    }

    @SneakyThrows(Exception.class)
    public StoragePath getStoragePath() {
        if (this.dataset != null) {
            return StoragePathBuilder.getInstance(
                    this.dataset.getDatasetStorageType(), this.dataset.getDatasetStoragePath());
        }
        if (StringUtils.isNotBlank(storageTypeStr)) {
            return StoragePathBuilder.getInstanceByFilePath(storageTypeStr, path);
        }
        return null;
    }

    public void setStorageTypeStr(String storageTypeStr) {
        this.storageTypeStr = storageTypeStr;
        this.storageType = StorageType.deserialize(storageTypeStr);
        resetType();
    }

    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
        if (this.storageType == null) {
            return;
        }
        resetType();
        this.storageTypeStr = storageType.getName();
    }

    public void resetType() {
        if (storageType == null) {
            return;
        }
        if (storageType.getName().compareToIgnoreCase(StorageType.HDFS.getName()) == 0) {
            this.type = FileStorageType.HDFS.getType();
        }
        if (storageType.getName().compareToIgnoreCase(StorageType.LOCAL.getName()) == 0) {
            this.type = FileStorageType.LocalFile.getType();
        }
    }

    @SneakyThrows(Exception.class)
    public void check(List<String> datasetIDList) {
        Common.requireNonEmpty(ownerAgency, "ownerAgency");
        // check datasetID valid
        if (this.datasetID != null && !this.datasetID.isEmpty()) {
            if ((datasetIDList != null)
                    && !datasetIDList.isEmpty()
                    && !datasetIDList.contains(this.datasetID)) {
                throw new WeDPRException(
                        "Invalid datasetID, datasetID must in datasetIDList set, datasetID: "
                                + datasetID);
            }
        }
    }

    @Override
    public String toString() {
        return "FileMeta{"
                + "type="
                + type
                + ", storageTypeStr='"
                + storageTypeStr
                + '\''
                + ", storageType="
                + storageType
                + ", path='"
                + path
                + '\''
                + ", owner='"
                + owner
                + '\''
                + ", ownerAgency='"
                + ownerAgency
                + '\''
                + '}';
    }
}
