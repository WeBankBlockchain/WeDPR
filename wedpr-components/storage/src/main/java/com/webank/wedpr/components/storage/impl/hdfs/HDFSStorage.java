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

package com.webank.wedpr.components.storage.impl.hdfs;

import com.webank.wedpr.components.storage.api.FileStorageInterface;
import com.webank.wedpr.components.storage.api.StorageMeta;
import com.webank.wedpr.components.storage.api.StoragePath;
import com.webank.wedpr.components.storage.config.HdfsStorageConfig;
import com.webank.wedpr.components.storage.stream.HdfsStorageStream;
import com.webank.wedpr.core.protocol.StorageType;
import com.webank.wedpr.core.utils.Common;
import com.webank.wedpr.core.utils.WeDPRException;
import java.io.Closeable;
import java.io.IOException;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(value = "wedpr.storage.type", havingValue = "HDFS")
@Component("fileStorage")
public class HDFSStorage implements FileStorageInterface {

    private static final Logger logger = LoggerFactory.getLogger(HDFSStorage.class);

    @Autowired private HdfsStorageConfig hdfsConfig;

    static class FsHandlerArgs implements Closeable {
        private final FileSystem fileSystem;
        private final HdfsStorageConfig hdfsConfig;

        @SneakyThrows
        public FsHandlerArgs(HdfsStorageConfig hdfsConfig) {
            try {
                Configuration hadoopConf = new Configuration();
                hadoopConf.set(StorageConstant.FS_URI_CONFIG_KEY, hdfsConfig.getUrl());
                this.fileSystem = FileSystem.get(hadoopConf);
                this.hdfsConfig = hdfsConfig;
                logger.info("connect to hdfs success, hdfsConfig: {}", hdfsConfig);
            } catch (Exception e) {
                logger.info("connect to hdfs failed, hdfsConfig: {}, error: ", hdfsConfig, e);
                throw new WeDPRException(
                        "connect to hdfs " + hdfsConfig + " failed, error: " + e.getMessage(), e);
            }
        }

        @Override
        public void close() {
            try {
                this.fileSystem.close();
            } catch (Exception e) {
                logger.error("close fs: {} failed, error: ", this.hdfsConfig, e);
                //                throw new WeDPRException(
                //                        "close fs "
                //                                + this.hdfsConfig
                //                                + " failed, error: "
                //                                + e.getMessage(),
                //                        e);
            }
        }

        public FileSystem getFileSystem() {
            return this.fileSystem;
        }
    }

    static class FsActionResult {

        private boolean fileExisted;
        private FileStatus fileStatus;

        public boolean isFileExisted() {
            return fileExisted;
        }

        public void setFileExisted(boolean fileExisted) {
            this.fileExisted = fileExisted;
        }

        public FileStatus getFileStatus() {
            return fileStatus;
        }

        public void setFileStatus(FileStatus fileStatus) {
            this.fileStatus = fileStatus;
        }
    }

    @FunctionalInterface
    interface FSHandler {
        void call(FsHandlerArgs args, FsActionResult result) throws IOException;
    }

    @SneakyThrows
    private void handleFsAction(FsActionResult result, String actionDesc, FSHandler fsHandler) {
        try (FsHandlerArgs fsHandlerArgs = new FsHandlerArgs(this.hdfsConfig)) {
            fsHandler.call(fsHandlerArgs, result);
        } catch (Exception e) {
            String errorMsg =
                    "handleFsAction for method " + actionDesc + " failed, error: " + e.getMessage();
            logger.error("{}, e: ", errorMsg, e);
            throw new WeDPRException(errorMsg, e);
        }
    }

    @Override
    public boolean exists(StoragePath storagePath) {
        HDFSStoragePath hdfsStoragePath = (HDFSStoragePath) storagePath;
        String filePath = hdfsStoragePath.getFilePath();

        FsActionResult result = new FsActionResult();
        handleFsAction(
                result,
                "exists",
                (fsHandlerArgs, fsActionResult) ->
                        fsActionResult.setFileExisted(
                                fsHandlerArgs.getFileSystem().exists(new Path(filePath))));
        return result.isFileExisted();
    }

    @SneakyThrows(Exception.class)
    public FileStatus getMetaInfo(String path) {
        FsActionResult result = new FsActionResult();
        handleFsAction(
                result,
                "getMetaInfo",
                (fsHandlerArgs, fsActionResult) ->
                        fsActionResult.setFileStatus(
                                fsHandlerArgs.getFileSystem().getFileStatus(new Path(path))));
        return result.getFileStatus();
    }

    /**
     * upload the file from localPath to remotePath
     *
     * @param localPath the source path
     * @param remotePath the remote dest-path
     * @param isAbsPath
     */
    @SneakyThrows(Exception.class)
    @Override
    public StoragePath upload(
            FilePermissionInfo filePermissionInfo,
            boolean enforceOverwrite,
            String localPath,
            String remotePath,
            boolean isAbsPath) {
        logger.debug("upload file: {}=>{}", localPath, remotePath);

        String remoteAbsPath;
        if (!isAbsPath) {
            remoteAbsPath = Common.joinPath(getBaseDir(), remotePath);
        } else {
            remoteAbsPath = remotePath;
        }

        handleFsAction(
                null,
                "upload",
                (fsHandlerArgs, fsActionResult) -> {
                    fsHandlerArgs
                            .getFileSystem()
                            .copyFromLocalFile(
                                    enforceOverwrite, new Path(localPath), new Path(remoteAbsPath));
                    if (filePermissionInfo == null) {
                        return;
                    }
                    String group = filePermissionInfo.getGroup();
                    if (StringUtils.isBlank(group)) {
                        group = hdfsConfig.getUser();
                    }
                    logger.info(
                            "upload data and set permission, remotePath: {}, user: {}, group: {}",
                            remotePath,
                            filePermissionInfo.getOwner(),
                            group);
                    // set the permission
                    fsHandlerArgs
                            .getFileSystem()
                            .setOwner(
                                    new Path(remoteAbsPath), filePermissionInfo.getOwner(), group);
                });

        HDFSStoragePath hdfsStoragePath = new HDFSStoragePath();
        hdfsStoragePath.setFilePath(remoteAbsPath);
        return hdfsStoragePath;
    }

    /**
     * download the file from remotePath to localPath
     *
     * @param storagePath the remote source path
     * @param localPath the local source path
     */
    @SneakyThrows
    @Override
    public void download(StoragePath storagePath, String localPath) {
        HDFSStoragePath hdfsStoragePath = (HDFSStoragePath) storagePath;
        String filePath = hdfsStoragePath.getFilePath();

        logger.debug("download file: {}=>{}", filePath, localPath);
        handleFsAction(
                null,
                "download",
                (fsHandlerArgs, fsActionResult) ->
                        fsHandlerArgs
                                .getFileSystem()
                                .copyToLocalFile(new Path(filePath), new Path(localPath)));
    }

    /**
     * delete the remote file
     *
     * @param storagePath
     */
    @SneakyThrows
    @Override
    public void delete(StoragePath storagePath) {
        HDFSStoragePath hdfsStoragePath = (HDFSStoragePath) storagePath;
        String filePath = hdfsStoragePath.getFilePath();

        logger.info("delete file: {}", filePath);
        handleFsAction(
                null,
                "delete",
                (fsHandlerArgs, fsActionResult) ->
                        fsHandlerArgs.getFileSystem().delete(new Path(filePath), true));
    }

    /**
     * move the remote file to given path
     *
     * @param sourceStoragePath
     * @param destStoragePath
     */
    @Override
    public void rename(StoragePath sourceStoragePath, StoragePath destStoragePath) {

        HDFSStoragePath sourceHdfsStoragePath = (HDFSStoragePath) sourceStoragePath;
        HDFSStoragePath destHdfsStoragePath = (HDFSStoragePath) destStoragePath;

        String sourceFilePath = sourceHdfsStoragePath.getFilePath();
        String destFilePath = destHdfsStoragePath.getFilePath();

        logger.info("rename: {}=>{}", sourceFilePath, destFilePath);
        handleFsAction(
                null,
                "download",
                (fsHandlerArgs, fsActionResult) ->
                        fsHandlerArgs
                                .getFileSystem()
                                .rename(new Path(sourceFilePath), new Path(destFilePath)));
    }

    @Override
    public StorageMeta getMeta(StoragePath storagePath) {
        HDFSStoragePath hdfsStoragePath = (HDFSStoragePath) storagePath;
        String filePath = hdfsStoragePath.getFilePath();

        FileStatus metaInfo = getMetaInfo(filePath);

        logger.error(
                "get hdfs storage meta, filePath: {}, length: {}", filePath, metaInfo.getLen());

        StorageMeta storageMeta = new StorageMeta();
        storageMeta.setLength(metaInfo.getLen());
        return storageMeta;
    }

    @Override
    public String getBaseDir() {
        return hdfsConfig.getBaseDir();
    }

    @SneakyThrows
    @Override
    public HdfsStorageStream open(StoragePath storagePath) {

        HDFSStoragePath hdfsStoragePath = (HDFSStoragePath) storagePath;
        String filePath = hdfsStoragePath.getFilePath();

        Configuration hadoopConf = new Configuration();
        hadoopConf.set(StorageConstant.FS_URI_CONFIG_KEY, hdfsConfig.getUrl());

        FileSystem fileSystem = FileSystem.get(hadoopConf);
        FSDataInputStream fsDataInputStream = fileSystem.open(new Path(filePath));

        logger.info("open hdfs storage stream, filePath: {}, ", filePath);
        return new HdfsStorageStream(fileSystem, fsDataInputStream);
    }

    @Override
    public StorageType type() {
        return StorageType.HDFS;
    }
}
