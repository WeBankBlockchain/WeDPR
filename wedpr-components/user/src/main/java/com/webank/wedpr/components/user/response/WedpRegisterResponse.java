package com.webank.wedpr.components.user.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/** Created by caryliao on 2024/7/18 16:58 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WedpRegisterResponse {
    private String username;
}
