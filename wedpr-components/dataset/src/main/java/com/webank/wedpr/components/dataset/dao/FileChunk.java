package com.webank.wedpr.components.dataset.dao;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FileChunk {
    /** 数据集id */
    private String datasetId;
    /** 文件md5 */
    private String identifier;
    /** 分块文件 */
    MultipartFile filesChunk;
    /** 当前分块序号 */
    private Integer index;
    /** 分块总数 */
    private Integer totalCount;

    @Override
    public String toString() {
        return "FileChunk{"
                + "datasetId='"
                + datasetId
                + '\''
                + ", identifier='"
                + identifier
                + '\''
                + ", index="
                + index
                + ", totalCount="
                + totalCount
                + ", filesChunk="
                + filesChunk.getSize()
                + '}';
    }
}
