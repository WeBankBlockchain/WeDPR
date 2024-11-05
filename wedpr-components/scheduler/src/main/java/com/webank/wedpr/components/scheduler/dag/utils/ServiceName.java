package com.webank.wedpr.components.scheduler.dag.utils;

public enum ServiceName {
    PSI("psi"),
    MODEL("model"),
    MPC("mpc");

    private String value;

    ServiceName(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
