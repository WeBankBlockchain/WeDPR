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

package com.webank.wedpr.components.storage.api;

import com.webank.wedpr.core.protocol.StorageType;
import lombok.Data;

public interface FileStorageInterface {

    @Data
    public static class FilePermissionInfo {
        private String owner;
        private String group;
    }

    /**
     * base dir
     *
     * @return
     */
    String getBaseDir();

    /**
     * open storage for input stream
     *
     * @param storagePath
     * @return
     */
    StorageStreamApi open(StoragePath storagePath);

    /**
     * check the file exists or not
     *
     * @param storagePath
     */
    boolean exists(StoragePath storagePath);

    /**
     * upload the file from localPath to remotePath
     *
     * @param enforceOverwrite
     * @param localPath the source path
     * @param remotePath the remote dest-path
     * @param isAbsPath
     */
    StoragePath upload(
            FilePermissionInfo filePermissionInfo,
            boolean enforceOverwrite,
            String localPath,
            String remotePath,
            boolean isAbsPath);

    /**
     * download the file from remotePath to localPath
     *
     * @param storagePath the storage source path
     * @param localPath the local source path
     */
    void download(StoragePath storagePath, String localPath);

    /**
     * delete the remote file
     *
     * @param storagePath
     */
    void delete(StoragePath storagePath);

    /**
     * @param storagePath
     * @return
     */
    StorageMeta getMeta(StoragePath storagePath);

    /**
     * move the remote file to given path
     *
     * @param sourceStoragePath
     * @param destStoragePath
     */
    void rename(StoragePath sourceStoragePath, StoragePath destStoragePath);

    StorageType type();
}
