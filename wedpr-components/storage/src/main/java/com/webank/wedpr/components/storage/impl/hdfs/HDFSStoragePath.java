package com.webank.wedpr.components.storage.impl.hdfs;

import com.webank.wedpr.components.storage.api.StoragePath;
import com.webank.wedpr.core.protocol.StorageType;
import com.webank.wedpr.core.utils.ObjectMapperFactory;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
@Setter
public class HDFSStoragePath extends StoragePath {

    protected HDFSStoragePath() {
        super(StorageType.HDFS.getName());
    }
    public HDFSStoragePath(String path){
        super(StorageType.HDFS.getName());
        this.filePath = Paths.get(path);
    }
}
