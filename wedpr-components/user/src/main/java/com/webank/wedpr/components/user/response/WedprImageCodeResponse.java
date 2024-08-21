package com.webank.wedpr.components.user.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WedprImageCodeResponse {
    String randomToken;
    String imageBase64;
}
