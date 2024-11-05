package com.webank.wedpr.components.scheduler.dag.base;

public class CycleFoundException extends RuntimeException {

    public CycleFoundException(String message) {
        super(message);
    }
}
