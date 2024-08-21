package com.webank.wedpr.components.dataset.sync;

public enum DatasetSyncerAction {
    CREATE("CreateDataset"),
    REMOVE("RemoveDataset"),
    UPDATE("UpdateDataset");
    private final String action;

    DatasetSyncerAction(String action) {
        this.action = action;
    }

    public String getAction() {
        return this.action;
    }
}
