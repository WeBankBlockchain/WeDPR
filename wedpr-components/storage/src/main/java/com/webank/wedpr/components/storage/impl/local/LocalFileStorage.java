package com.webank.wedpr.components.storage.impl.local;

import com.webank.wedpr.common.protocol.StorageType;
import com.webank.wedpr.common.utils.Common;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import com.webank.wedpr.components.storage.api.StorageMeta;
import com.webank.wedpr.components.storage.api.StoragePath;
import com.webank.wedpr.components.storage.config.LocalStorageConfig;
import com.webank.wedpr.components.storage.stream.LocalStorageStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import lombok.Data;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(value = "wedpr.storage.type", havingValue = "LOCAL", matchIfMissing = true)
@Component("fileStorage")
@Data
public class LocalFileStorage implements FileStorageInterface {

    static final Logger logger = LoggerFactory.getLogger(LocalFileStorage.class);

    @Autowired LocalStorageConfig localStorageConfig;

    /**
     * create directory if not exist
     *
     * @param dir
     */
    public void createDirIfNotExist(String dir) throws IOException {
        File baseDirFile = new File(dir);
        if (!baseDirFile.exists()) {
            // Files.createDirectory(baseDirFile.toPath());
            boolean mkdirs = baseDirFile.mkdirs();
            if (!mkdirs) {
                logger.error("failed to create directory, dir: {}, mkdir: {}", dir, mkdirs);
                throw new IOException("failed to create directory, dir: " + dir);
            }
            logger.info("create directory {}", dir);
        }
    }

    public boolean exists(String path) {
        File file = new File(path);
        return file.exists();
    }

    @Override
    public boolean exists(StoragePath storagePath) {
        LocalStoragePath localStoragePath = (LocalStoragePath) storagePath;
        String filePath = localStoragePath.getFilePath();
        return exists(filePath);
    }

    @Override
    public StoragePath upload(
            FilePermissionInfo filePermissionInfo,
            boolean enforceOverwrite,
            String localPath,
            String remotePath,
            boolean isAbsPath) {
        LocalStoragePath localStoragePath = new LocalStoragePath();

        String remoteAbsPath;
        if (!isAbsPath) {
            remoteAbsPath = Common.joinPath(getBaseDir(), remotePath);
        } else {
            remoteAbsPath = remotePath;
        }

        Path source = Paths.get(localPath);
        Path target = Paths.get(remoteAbsPath);

        try {
            String absolutePath = target.getParent().toFile().getAbsolutePath();
            createDirIfNotExist(absolutePath);

            if (target.toFile().exists() && Files.isSameFile(source, target)) {
                localStoragePath.setFilePath(remoteAbsPath);
                return localStoragePath;
            }

            if (enforceOverwrite) {
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.copy(source, target);
            }
        } catch (IOException e) {
            logger.error("upload failed, e", e);
            throw new RuntimeException(e);
        }
        localStoragePath.setFilePath(remoteAbsPath);
        return localStoragePath;
    }

    @Override
    public void download(StoragePath storagePath, String localPath) {
        LocalStoragePath localStoragePath = (LocalStoragePath) storagePath;
        String filePath = localStoragePath.getFilePath();

        try {

            Path source = Paths.get(filePath);
            Path target = Paths.get(localPath);
            if (target.toFile().exists() && Files.isSameFile(source, target)) {
                return;
            }

            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error("e", e);
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows(WeDPRException.class)
    @Override
    public void delete(StoragePath storagePath) {
        LocalStoragePath localStoragePath = (LocalStoragePath) storagePath;

        String filePath = localStoragePath.getFilePath();
        File file = new File(filePath);
        if (!file.delete()) {
            throw new WeDPRException("delete " + storagePath + " failed!");
        }
    }

    @SneakyThrows
    @Override
    public StorageMeta getMeta(StoragePath storagePath) {
        LocalStoragePath localStoragePath = (LocalStoragePath) storagePath;
        String filePath = localStoragePath.getFilePath();

        File file = new File(filePath);

        if (!file.exists() || !file.isFile()) {
            logger.error("file does not exist, filePath: {}", filePath);
            throw new WeDPRException("file does not exist, path: " + filePath);
        }

        long length = file.length();

        logger.info("get local storage meta, filePath: {}, length: {}", filePath, length);

        StorageMeta storageMeta = new StorageMeta();
        storageMeta.setLength(length);
        return storageMeta;
    }

    @Override
    public void rename(StoragePath sourceStoragePath, StoragePath destStoragePath) {
        LocalStoragePath destLocalStoragePath = (LocalStoragePath) destStoragePath;
        String destStorageAbsPath =
                localStorageConfig.getStorageAbsPath(destLocalStoragePath.getFilePath());
        download(sourceStoragePath, destStorageAbsPath);
        delete(sourceStoragePath);
    }

    @Override
    public String getBaseDir() {
        return localStorageConfig.getBaseDir();
    }

    @SneakyThrows
    @Override
    public LocalStorageStream open(StoragePath storagePath) {

        LocalStoragePath localStoragePath = (LocalStoragePath) storagePath;
        String filePath = localStoragePath.getFilePath();

        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            logger.error("file does not exist, filePath: {}", filePath);
            throw new WeDPRException("file does not exist, path: " + filePath);
        }

        logger.info("open local storage stream, filePath: {}", filePath);

        BufferedInputStream bufferedInputStream =
                new BufferedInputStream(Files.newInputStream(Paths.get(filePath)));
        return new LocalStorageStream(bufferedInputStream);
    }

    @Override
    public StorageType type() {
        return StorageType.LOCAL;
    }
}
