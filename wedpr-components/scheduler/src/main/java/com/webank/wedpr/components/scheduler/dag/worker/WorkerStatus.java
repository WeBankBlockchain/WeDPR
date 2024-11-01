package com.webank.wedpr.components.scheduler.dag.worker;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum WorkerStatus {
    PENDING("PENDING"),
    RUNNING("RUNNING"),
    FAILURE("FAILURE"),
    KILLED("KILLED"),
    SUCCESS("SUCCESS");

    private static final Logger logger = LoggerFactory.getLogger(WorkerStatus.class);
    private final String status;

    WorkerStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }

    public boolean isFailed() {
        return ordinal() == WorkerStatus.FAILURE.ordinal();
    }

    public boolean isKilled() {
        return ordinal() == WorkerStatus.KILLED.ordinal();
    }

    public boolean isSuccess() {
        return ordinal() == WorkerStatus.SUCCESS.ordinal();
    }

    public static WorkerStatus deserialize(String status) {
        if (StringUtils.isBlank(status)) {
            return null;
        }
        for (WorkerStatus workerStatus : WorkerStatus.values()) {
            if (workerStatus.status.compareToIgnoreCase(status) == 0) {
                return workerStatus;
            }
        }
        return null;
    }
}
