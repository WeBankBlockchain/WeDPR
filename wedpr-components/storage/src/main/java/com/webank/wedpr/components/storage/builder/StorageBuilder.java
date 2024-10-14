package com.webank.wedpr.components.storage.builder;

import com.webank.wedpr.common.protocol.StorageType;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import com.webank.wedpr.components.storage.impl.hdfs.HDFSStorage;
import com.webank.wedpr.components.storage.impl.local.LocalFileStorage;
import java.util.HashMap;
import java.util.Map;

public class StorageBuilder {

    private StorageBuilder() {}

    private static final Map<String, Class<?>> type2Class = new HashMap<>();

    static {
        type2Class.put(StorageType.HDFS.getName(), HDFSStorage.class);
        type2Class.put(StorageType.LOCAL.getName(), LocalFileStorage.class);
    }

    public static FileStorageInterface getInstance(String storageType)
            throws InstantiationException, IllegalAccessException {
        Class<?> aClass = type2Class.get(storageType.toUpperCase());
        return (FileStorageInterface) aClass.newInstance();
    }
}
