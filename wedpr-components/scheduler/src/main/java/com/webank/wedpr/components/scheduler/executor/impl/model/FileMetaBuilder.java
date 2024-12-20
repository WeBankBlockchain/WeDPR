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

import com.webank.wedpr.common.protocol.StorageType;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.components.storage.builder.StoragePathBuilder;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

public class FileMetaBuilder {
    private final StoragePathBuilder storagePathBuilder;

    public FileMetaBuilder(StoragePathBuilder storagePathBuilder) {
        this.storagePathBuilder = storagePathBuilder;
    }

    @SneakyThrows(Exception.class)
    public FileMeta build(StorageType storageType, String path, String owner, String agency) {
        return new FileMeta(storageType, path, owner, agency);
    }

    @SneakyThrows(Exception.class)
    public FileMeta build(String data) {
        if (StringUtils.isBlank(data)) {
            return null;
        }
        return ObjectMapperFactory.getObjectMapper().readValue(data, FileMeta.class);
    }

    // add baseDir to the filePath
    public void getAbsoluteDir(FileMeta fileMeta) {
        String pathWithHome =
                storagePathBuilder.getAbsoluteDir(
                        fileMeta.getStorageType().getName(), fileMeta.getPath());
        fileMeta.setPath(pathWithHome);
    }
}
