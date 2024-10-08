package com.webank.wedpr.components.db.mapper.service.publish.model;

import org.apache.commons.lang3.StringUtils;

public enum ServiceStatus {
    Publishing("Publishing"),
    PublishFailed("PublishFailed"),
    PublishSuccess("PublishSuccess");

    private final String status;

    ServiceStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }

    public boolean isReady() {
        return ordinal() == ServiceStatus.PublishSuccess.ordinal();
    }

    public static ServiceStatus deserialize(String status) {
        if (StringUtils.isBlank(status)) {
            return null;
        }
        for (ServiceStatus serviceStatus : ServiceStatus.values()) {
            if (serviceStatus.status.compareToIgnoreCase(status) == 0) {
                return serviceStatus;
            }
        }
        return null;
    }
}
