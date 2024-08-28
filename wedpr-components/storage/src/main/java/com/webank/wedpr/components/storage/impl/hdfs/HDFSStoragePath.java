package com.webank.wedpr.components.storage.impl.hdfs;

import com.webank.wedpr.components.storage.api.StoragePath;
import com.webank.wedpr.core.protocol.StorageType;
import java.nio.file.Paths;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HDFSStoragePath extends StoragePath {

    protected HDFSStoragePath() {
        super(StorageType.HDFS.getName());
    }

    public HDFSStoragePath(String path) {
        super(StorageType.HDFS.getName());
        this.filePath = Paths.get(path);
    }
}
