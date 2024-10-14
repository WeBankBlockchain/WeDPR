package com.webank.wedpr.components.user.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/** Created by caryliao on 2024/7/29 22:25 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WedprUserDTO {
    private String username;
}
