package com.webank.wedpr.components.dataset.dao;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MergeChunkResult {
    // merged file path
    private String mergedFilePath;
    // dataset data size
    private long datasetSize;
    // dataset version hash
    private String datasetVersionHash;
}
