package com.webank.wedpr.components.dataset.message;

import lombok.Data;

@Data
public class DownloadFileShardRequest {

    private Integer shardCount;
    private Integer shardIndex;
    private String filePath;
}
