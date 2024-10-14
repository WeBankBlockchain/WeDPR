package com.webank.wedpr.components.user.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
public class WedprImageCodeResponse {
    String randomToken;
    String imageBase64;
}
