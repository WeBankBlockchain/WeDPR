package com.webank.wedpr.components.storage.impl.local;

import com.webank.wedpr.components.storage.api.StoragePath;
import com.webank.wedpr.core.protocol.StorageType;
import java.nio.file.Paths;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocalStoragePath extends StoragePath {
    public LocalStoragePath() {
        super(StorageType.LOCAL.getName());
    }

    public LocalStoragePath(String path) {
        super(StorageType.LOCAL.getName());
        this.filePath = Paths.get(path);
    }
}
