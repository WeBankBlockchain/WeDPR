package com.webank.wedpr.components.publish.entity.request;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * @author zachma
 * @date 2024/8/31
 */
@Data
public class NodeInvokeRequest {
    private String serviceId;
    private String invokeAgency;
    private String invokeUser;
    private String invokeStatus;
    private LocalDateTime invokeTime;
}
