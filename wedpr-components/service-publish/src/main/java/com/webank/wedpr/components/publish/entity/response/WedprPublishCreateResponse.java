package com.webank.wedpr.components.publish.entity.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author zachma
 * @date 2024/8/31
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
public class WedprPublishCreateResponse {
    private String serviceId;
}
