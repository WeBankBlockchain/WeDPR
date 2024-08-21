package com.webank.wedpr.components.storage.api;

import com.webank.wedpr.core.utils.ObjectMapperFactory;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
@Setter
public abstract class StoragePath {
    protected final String storageType;
    protected Path filePath;

    protected StoragePath(String storageType) {
        this.storageType = storageType;
    }

    public String getFilePath() {
        return this.filePath.toString();
    }

    public void setFilePath(String filePath){
        if(filePath == null){
            return;
        }
        this.filePath = Paths.get(filePath);
    }
}
