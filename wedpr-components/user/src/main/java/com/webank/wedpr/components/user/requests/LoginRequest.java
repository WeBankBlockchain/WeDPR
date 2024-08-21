package com.webank.wedpr.components.user.requests;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
    private String randomToken;
    private String imageCode;
    private String mailCode;
}
