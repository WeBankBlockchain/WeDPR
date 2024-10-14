package com.webank.wedpr.components.storage.builder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.webank.wedpr.common.protocol.StorageType;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.storage.api.StoragePath;
import com.webank.wedpr.components.storage.config.HdfsStorageConfig;
import com.webank.wedpr.components.storage.config.LocalStorageConfig;
import com.webank.wedpr.components.storage.impl.hdfs.HDFSStoragePath;
import com.webank.wedpr.components.storage.impl.local.LocalStoragePath;
import java.util.HashMap;
import java.util.Map;
import lombok.SneakyThrows;

public class StoragePathBuilder {

    private final HdfsStorageConfig hdfsConfig;
    private final LocalStorageConfig localStorageConfig;

    public StoragePathBuilder(HdfsStorageConfig hdfsConfig, LocalStorageConfig localStorageConfig) {
        this.hdfsConfig = hdfsConfig;
        this.localStorageConfig = localStorageConfig;
    }

    private static final Map<String, Class<?>> type2Class = new HashMap<>();

    static {
        type2Class.put(StorageType.HDFS.getName(), HDFSStoragePath.class);
        type2Class.put(StorageType.LOCAL.getName(), LocalStoragePath.class);
        // TODO: add others
    }

    public static StoragePath getInstance(String storageType, String strStoragePath)
            throws JsonProcessingException, WeDPRException {
        Class<?> aClass = type2Class.get(storageType.toUpperCase());
        if (aClass == null) {
            throw new WeDPRException("Unsupported storage type , value : " + storageType);
        }
        return (StoragePath)
                ObjectMapperFactory.getObjectMapper().readValue(strStoragePath, aClass);
    }

    public static StoragePath getInstanceByFilePath(String storageType, String filePath)
            throws WeDPRException {
        if (storageType.compareToIgnoreCase(StorageType.HDFS.getName()) == 0) {
            return new HDFSStoragePath(filePath);
        }
        if (storageType.compareToIgnoreCase(StorageType.LOCAL.getName()) == 0) {
            return new LocalStoragePath(filePath);
        }
        throw new WeDPRException("getInstanceByFilePath: Invalid storageType: " + storageType);
    }

    @SneakyThrows(Exception.class)
    public String getAbsoluteDir(String storageType, String filePath) {
        if (storageType.compareToIgnoreCase(StorageType.HDFS.getName()) == 0) {
            return hdfsConfig.getAbsPathInHdfs(filePath);
        }
        if (storageType.compareToIgnoreCase(StorageType.LOCAL.getName()) == 0) {
            return localStorageConfig.getStorageAbsPath(filePath);
        }
        throw new WeDPRException("getPathWithHome: Invalid storageType: " + storageType);
    }
}
