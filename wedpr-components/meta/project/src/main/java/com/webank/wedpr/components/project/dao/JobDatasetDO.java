package com.webank.wedpr.components.project.dao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/** Created by caryliao on 2024/9/6 22:43 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobDatasetDO {
    private String jobId;
    private String datasetId;
    private Integer reportStatus;
    private String createTime;
    private Integer limitItems;
}
