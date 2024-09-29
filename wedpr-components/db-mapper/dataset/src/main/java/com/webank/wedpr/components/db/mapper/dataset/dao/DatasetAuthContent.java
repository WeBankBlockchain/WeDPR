package com.webank.wedpr.components.db.mapper.dataset.dao;

import lombok.Data;

@Data
public class DatasetAuthContent {
    private String datasetId;
    private String datasetTitle;
    private String ownerUserName;
    private String ownerAgencyName;
    private String authTime;
    private Integer permissionType;

    private String user;
    private String agency;
}
