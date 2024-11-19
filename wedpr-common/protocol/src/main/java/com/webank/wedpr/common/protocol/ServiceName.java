package com.webank.wedpr.common.protocol;

public enum ServiceName {
    PSI("psi"),
    MODEL("model"),
    MPC("mpc"),
    PIR("pir");

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
