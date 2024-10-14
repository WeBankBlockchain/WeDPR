package com.webank.wedpr.components.dataset.service;

import com.alibaba.druid.util.HexBin;
import com.webank.wedpr.common.protocol.StorageType;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.dataset.config.DatasetConfig;
import com.webank.wedpr.components.db.mapper.dataset.dao.UserInfo;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import com.webank.wedpr.components.storage.api.StorageMeta;
import com.webank.wedpr.components.storage.api.StoragePath;
import com.webank.wedpr.components.storage.api.StorageStreamApi;
import com.webank.wedpr.components.storage.builder.StoragePathBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("downloadService")
@Data
public class DownloadServiceImpl implements DownloadServiceApi {

    private static final Logger logger = LoggerFactory.getLogger(DownloadServiceImpl.class);

    @Autowired private DatasetConfig datasetConfig;

    @Qualifier("fileStorage")
    @Autowired
    private FileStorageInterface fileStorage;

    // calculate shard count
    public static int calculateShardCount(long fileSize, int shardSize) {
        int fullShards = (int) (fileSize / shardSize);
        if (fileSize % shardSize != 0) {
            fullShards++;
        }
        return fullShards;
    }

    @Override
    public int getFileShardsInfo(UserInfo userInfo, String filePath) throws WeDPRException {

        StorageType storageType = fileStorage.type();

        int shardSize = datasetConfig.getShardSize();

        StoragePath storagePath =
                StoragePathBuilder.getInstanceByFilePath(storageType.getName(), filePath);
        StorageMeta storageMeta = fileStorage.getMeta(storagePath);

        long length = storageMeta.getLength();
        int shardCount = calculateShardCount(length, shardSize);

        logger.info(
                "get file shards info end, shardCount: {}, shardSize: {}, storageType: {}, filePath: {}, fileLength: {}",
                shardCount,
                shardSize,
                storageType,
                filePath,
                length);

        return shardCount;
    }

    public int copyStream(
            InputStream inputStream,
            OutputStream outputStream,
            int shouldReadSize,
            MessageDigest messageDigest)
            throws IOException {

        byte[] buffer = new byte[1024 * 10];
        int leftSize = shouldReadSize;
        int totalReadSize = 0;

        while (leftSize > 0) {
            int readSize = Math.min(leftSize, buffer.length);
            int ret = inputStream.read(buffer, 0, readSize);
            if (ret < 0) {
                logger.info("read the end of the file, ret < 0, ret: {}", ret);
                break;
            }

            if (messageDigest != null) {
                messageDigest.update(buffer, 0, ret);
            }

            outputStream.write(buffer, 0, ret);
            leftSize -= ret;
            totalReadSize += ret;
        }

        if (logger.isInfoEnabled()) {
            logger.info("shouldReadSize: {}, totalReadSize: {}", shouldReadSize, totalReadSize);
        }

        return totalReadSize;
    }

    @Override
    public void downloadFileShardData(
            UserInfo userInfo,
            String filePath,
            int totalShardCount,
            int shardIndex,
            OutputStream outputStream)
            throws WeDPRException {
        long startTimeMillis = System.currentTimeMillis();

        StorageType storageType = fileStorage.type();

        logger.info(
                "download file shards data, user: {}, filePath: {}, storageType: {}, shardIndex:{}",
                userInfo,
                filePath,
                storageType,
                shardIndex);

        StoragePath storagePath =
                StoragePathBuilder.getInstanceByFilePath(storageType.getName(), filePath);
        try (StorageStreamApi storageStreamApi = fileStorage.open(storagePath)) {
            long fileLength = storageStreamApi.length();
            int shardSize = datasetConfig.getShardSize();
            int shardCount = calculateShardCount(fileLength, shardSize);

            logger.info(
                    "file shards info, filePath: {}, fileLength: {}, shardSize: {}, shardCount: {}, shardIndex: {}, storageType: {}",
                    filePath,
                    fileLength,
                    shardSize,
                    shardCount,
                    shardIndex,
                    storageType);

            if (shardIndex >= shardCount) {
                logger.error(
                        "download file request shard index overflow, filePath: {}, fileLength: {}, shardIndex: {}, shardCount: {}, shardIndex: {}, storageType: {}",
                        filePath,
                        fileLength,
                        shardIndex,
                        shardCount,
                        shardIndex,
                        storageType);
                throw new WeDPRException(
                        "download file request shard index overflow, shardIndex: "
                                + shardIndex
                                + " , shardCount: "
                                + shardCount);
            }

            long fileReadOffset = (long) shardIndex * shardSize;

            long skip = storageStreamApi.skip(fileReadOffset);
            MessageDigest messageDigest = null;
            try {
                messageDigest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                logger.warn("NoSuchAlgorithmException, e: ", e);
            }

            int totalReadSize =
                    copyStream(storageStreamApi.stream(), outputStream, shardSize, messageDigest);

            String hexHashValue = null;
            if (messageDigest != null) {
                hexHashValue = HexBin.encode(messageDigest.digest(), false);
            }

            long endTimeMillis = System.currentTimeMillis();

            logger.info(
                    "download transfer file data end, filePath: {}, fileLength: {}, shardSize: {}, shardCount: {}, shardIndex: {}, storageType: {}, skip: {}, totalReadSize: {}, hexHashValue: {}, cost(ms): {}",
                    filePath,
                    fileLength,
                    shardSize,
                    shardCount,
                    shardIndex,
                    storageType,
                    skip,
                    totalReadSize,
                    hexHashValue,
                    (endTimeMillis - startTimeMillis));

        } catch (IOException e) {
            logger.debug("IOException, e: ", e);
            throw new WeDPRException(e.getMessage());
        }
    }
}
