package com.webank.wedpr.components.authorization.core;

import lombok.Getter;

@Getter
public enum AuthApplyType {
    DATASET("wedpr_data_auth"),
    SERVICE("wedpr_service_auth");

    private final String type;

    AuthApplyType(String type) {
        this.type = type;
    }
}
