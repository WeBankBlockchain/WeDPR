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
import com.webank.wedpr.components.storage.api.StoragePath;
import com.webank.wedpr.components.storage.builder.StoragePathBuilder;
import com.webank.wedpr.core.protocol.StorageType;
import com.webank.wedpr.core.utils.Common;
import com.webank.wedpr.core.utils.ObjectMapperFactory;
import com.webank.wedpr.core.utils.WeDPRException;
import java.util.List;
import lombok.SneakyThrows;

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
    protected Integer type;
    protected String storageTypeStr;
    @JsonIgnore protected transient StorageType storageType;

    // Note: path works only if the dataset id is not specified
    protected String path;
    protected String owner;
    protected String ownerAgency;

    public FileMeta() {}

    public FileMeta(StorageType storageType, String path, String owner, String ownerAgency) {
        setStorageType(storageType);
        this.path = path;
        this.owner = owner;
        this.ownerAgency = ownerAgency;
    }

    public String serialize() throws Exception {
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwnerAgency() {
        return ownerAgency;
    }

    public void setOwnerAgency(String ownerAgency) {
        this.ownerAgency = ownerAgency;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String filePath) {
        this.path = filePath;
    }

    @SneakyThrows(Exception.class)
    public StoragePath getStoragePath() {
        return StoragePathBuilder.getInstanceByFilePath(storageTypeStr, path);
    }

    public String getStorageTypeStr() {
        return storageTypeStr;
    }

    public void setStorageTypeStr(String storageTypeStr) {
        this.storageTypeStr = storageTypeStr;
        this.storageType = StorageType.deserialize(storageTypeStr);
        resetType();
    }

    public StorageType getStorageType() {
        return storageType;
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
        Common.requireNonEmpty(owner, "owner");
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
        } else {
            if (this.storageType == null) {
                throw new WeDPRException("Not supported storageType: " + storageTypeStr);
            }
            Common.requireNonEmpty(path, "filePath");
        }
    }

    public String getDatasetID() {
        return datasetID;
    }

    public void setDatasetID(String datasetID) {
        this.datasetID = datasetID;
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
