package com.webank.wedpr.components.dataset.service;

import com.alibaba.druid.util.HexBin;
import com.webank.wedpr.components.dataset.config.DatasetConfig;
import com.webank.wedpr.components.dataset.dao.FileChunk;
import com.webank.wedpr.components.dataset.dao.MergeChunkResult;
import com.webank.wedpr.components.dataset.message.MergeChunkRequest;
import com.webank.wedpr.components.db.mapper.dataset.exception.DatasetException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import lombok.Data;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Chunked Upload Service */
@Component("chunkUpload")
@Data
public class ChunkUploadImpl implements ChunkUploadApi {

    private static final Logger logger = LoggerFactory.getLogger(ChunkUploadImpl.class);

    public static final String UPLOAD_CHUNK_FILE_NAME_PREFIX = "chunks";
    public static final String UPLOAD_MERGED_FILE_NAME_PREFIX = "merged";

    @Autowired private DatasetConfig datasetConfig;

    // ${dir}/chunks/${datasetId}/${identifier}
    public String getUploadFileDirPath(String datasetId, String identifier) {
        String datasetChunksDir = datasetConfig.getDatasetChunksDir(datasetId);
        return String.format("%s/%s", datasetChunksDir, identifier);
    }

    // ${dir}/dataset/chunks/${datasetId}/${identifier}/merged-${identifier}
    public String getMergedFilePath(String datasetId, String identifier) {
        String uploadFilePath = getUploadFileDirPath(datasetId, identifier);
        return String.format(
                "%s/%s-%s", uploadFilePath, UPLOAD_MERGED_FILE_NAME_PREFIX, identifier);
    }

    // ${dir}/chunks/${datasetId}/${identifier}/${count}-${index}
    public String getUploadChunkFilePath(
            String datasetId, String identifier, int chunkCount, int index) {
        String uploadFilePath = getUploadFileDirPath(datasetId, identifier);
        return String.format("%s/%d-%d", uploadFilePath, chunkCount, index);
    }

    /**
     * 检查文件是否存在
     *
     * @param pathStr
     * @return
     */
    public static boolean checkFileExistence(String pathStr) throws DatasetException {
        try {
            Path path = Paths.get(pathStr);
            return Files.exists(path);
        } catch (Exception e) {
            logger.error("File existence check exception, path: {} ,e: ", pathStr, e);
            throw new DatasetException(
                    "File existence check exception, path: " + pathStr + " ,e: " + e.getMessage());
        }
    }

    @Override
    public void uploadChunkData(FileChunk fileChunk) throws DatasetException {

        //      分片上传格式:
        //        filesChunk: (二进制)
        //        totalCount: 1
        //        index: 0
        //        identifier: 776dd53d59e2d67863f4b9a8bee7b542
        //        datasetId: d-xxxxxx

        // TODO: 优化项，如何正确的清理异常分片数据，目前思路是定时任务清理

        String datasetId = fileChunk.getDatasetId();
        String identifier = fileChunk.getIdentifier();
        int totalCount = fileChunk.getTotalCount();
        int index = fileChunk.getIndex();

        // 文件存储的目录
        String uploadFileDirPath = getUploadFileDirPath(datasetId, identifier);

        boolean fileExistence = checkFileExistence(uploadFileDirPath);
        if (!fileExistence) {
            File file = new File(uploadFileDirPath);
            boolean suc = file.mkdirs();
            logger.info(
                    "create chunk data dir, datasetId: {}, identifier: {}, totalCount: {}, index: {}, mkdirs: {}",
                    datasetId,
                    identifier,
                    totalCount,
                    index,
                    suc);
        }

        // 分片数据存储的路径
        String uploadChunkDataFilePath =
                getUploadChunkFilePath(datasetId, identifier, totalCount, index);

        // 写入分片
        try (InputStream inputStream = fileChunk.getFilesChunk().getInputStream();
                FileOutputStream outputStream = new FileOutputStream(uploadChunkDataFilePath)) {
            IOUtils.copy(inputStream, outputStream);
            logger.info(
                    "save the chunk data success, path: {},datasetId: {}, identifier: {}, totalCount: {}, index: {}",
                    uploadChunkDataFilePath,
                    datasetId,
                    identifier,
                    totalCount,
                    index);
        } catch (Exception e) {
            logger.error(
                    "failed to chunk shard data, path: {}, datasetId: {},identifier: {}, totalCount: {}, index: {}, e: ",
                    uploadChunkDataFilePath,
                    datasetId,
                    identifier,
                    totalCount,
                    index,
                    e);
            throw new DatasetException(e.getMessage());
        }
    }

    @Override
    public MergeChunkResult mergeChunkData(MergeChunkRequest mergeChunkRequest)
            throws DatasetException {

        long startTimeMillis = System.currentTimeMillis();

        String datasetId = mergeChunkRequest.getDatasetId();
        String identifier = mergeChunkRequest.getIdentifier();
        int totalChunkCount = mergeChunkRequest.getTotalCount();
        String hashAlgorithm = mergeChunkRequest.getIdentifierHash();
        String datasetVersionHashAlg = mergeChunkRequest.getDatasetVersionHash();

        long datasetSize = 0;
        //  MessageDigest Instance
        MessageDigest identifierMessageDigest = null;
        try {
            identifierMessageDigest = MessageDigest.getInstance(hashAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            logger.error(hashAlgorithm + " algorithm is not supported, e: ", e);
            throw new DatasetException(hashAlgorithm + " algorithm is not supported");
        }

        MessageDigest datasetVersionMessageDigest = null;
        try {
            datasetVersionMessageDigest = MessageDigest.getInstance(hashAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            logger.error(datasetVersionHashAlg + " algorithm is not supported, e: ", e);
            throw new DatasetException(datasetVersionHashAlg + " algorithm is not supported");
        }

        // 文件存储的目录
        String mergedFilePath = getMergedFilePath(datasetId, identifier);

        // 文件锁
        FileLock fileLock = null;
        // 合并文件
        try (FileOutputStream mergedFileOutputStream = new FileOutputStream(mergedFilePath)) {

            // 检查分片是否完整
            for (int index = 0; index < totalChunkCount; ++index) {
                String chunkFilePath =
                        getUploadChunkFilePath(datasetId, identifier, totalChunkCount, index);
                if (!checkFileExistence(chunkFilePath)) {
                    logger.error(
                            "missing upload chunk data, identifier:{}, totalCount: {}, index:{}",
                            identifier,
                            totalChunkCount,
                            index);
                    throw new DatasetException(
                            String.format(
                                    "missing upload chunk data, identifier: %s, totalCount: %d, index: %d",
                                    identifier, totalChunkCount, index));
                }
            }

            try {
                FileChannel channel = mergedFileOutputStream.getChannel();
                // lock file for prevent duplicate operations
                fileLock = channel.tryLock();
            } catch (Exception e) {
                logger.error(
                        "failed to lock the file lock in merge chunk data operation, identifier: {}, totalCount: {}, e: ",
                        identifier,
                        totalChunkCount,
                        e);
                throw new DatasetException(
                        "failed to lock the file lock in merge chunk data operation, e: "
                                + e.getMessage());
            }

            for (int index = 0; index < totalChunkCount; ++index) {
                String uploadChunkDataFilePath =
                        getUploadChunkFilePath(datasetId, identifier, totalChunkCount, index);
                byte[] readBytes = Files.readAllBytes(Paths.get(uploadChunkDataFilePath));
                identifierMessageDigest.update(readBytes);
                datasetVersionMessageDigest.update(readBytes);
                mergedFileOutputStream.write(readBytes);
                datasetSize += readBytes.length;
            }
        } catch (DatasetException datasetException) {
            throw datasetException;
        } catch (Exception e) {
            logger.error(
                    "merge chunk data exception, identifier:{}, totalCount: {}, e: ",
                    identifier,
                    totalChunkCount,
                    e);
            throw new DatasetException("merge chunk data exception, e: " + e.getMessage());
        }
        //        finally {
        //            if (fileLock != null) {
        //                try {
        //                    fileLock.release();
        //                } catch (IOException e) {
        //                    logger.warn(
        //                            "failed to unlock the file lock, identifier: {}, e: ",
        // identifier, e);
        //                }
        //            }
        //        }

        if (identifier.startsWith("0x") || identifier.startsWith("0X")) {
            identifier = identifier.substring(2);
        }

        // compare and verify MD5
        String hexDigest = HexBin.encode(identifierMessageDigest.digest(), false);
        if (!hexDigest.equalsIgnoreCase(identifier)) {
            logger.error(
                    "hash value mismatch, identifier: {}, hexDigest: {}", identifier, hexDigest);
            throw new DatasetException("Hash value mismatch");
        }

        String datasetVersionHexHash = HexBin.encode(datasetVersionMessageDigest.digest(), false);

        long endTimeMillis = System.currentTimeMillis();

        logger.info(
                "merge chunk data success, identifier: {}, totalCount: {}, cost(ms): {}",
                identifier,
                totalChunkCount,
                (endTimeMillis - startTimeMillis));

        return MergeChunkResult.builder()
                .mergedFilePath(mergedFilePath)
                .datasetSize(datasetSize)
                .datasetVersionHash(datasetVersionHexHash)
                .build();
    }

    @Override
    public void cleanChunkData(String datasetId, String identifier) {

        String dirPath = datasetConfig.getDatasetChunksDir(datasetId);

        try {
            FileUtils.deleteDirectory(new File(dirPath));
            logger.info(
                    "delete directory success, datasetId: {}, identifier: {}",
                    datasetId,
                    identifier);
        } catch (IOException e) {
            logger.warn(
                    "delete directory failed, datasetId: {}, identifier: {}",
                    datasetId,
                    identifier);
        }
    }
}
