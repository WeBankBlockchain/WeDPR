package com.webank.wedpr.components.publish.entity.request;

import lombok.Data;

/**
 * @author zachma
 * @date 2024/8/26
 */
@Data
public class PublishRequest {
    private String serviceId;
    private String serviceName;
    private String serviceDesc;
    private String serviceConfig;
    private String serviceType;
}
