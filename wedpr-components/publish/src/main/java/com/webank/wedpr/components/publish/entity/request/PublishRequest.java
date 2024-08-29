package com.webank.wedpr.components.publish.entity.request;

import lombok.Data;

/**
 * @author zachma
 * @date 2024/8/26
 */
@Data
public class PublishRequest {
    private String publishId;
    private String publishName;
    private String publishContent;
    private String datasetId;
    private String publishType;
    private String searchRule;
}
